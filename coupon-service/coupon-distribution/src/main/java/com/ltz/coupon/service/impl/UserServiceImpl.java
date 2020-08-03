package com.ltz.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.ltz.coupon.constant.Constant;
import com.ltz.coupon.constant.CouponStatus;
import com.ltz.coupon.dao.CouponDao;
import com.ltz.coupon.entity.Coupon;
import com.ltz.coupon.exception.CouponException;
import com.ltz.coupon.feign.SettlementClient;
import com.ltz.coupon.feign.TemplateClient;
import com.ltz.coupon.service.IRedisService;
import com.ltz.coupon.service.IUserService;
import com.ltz.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <h1>用户服务相关的接口实现</h1>
 * 所有的操作过程, 状态都保存在 Redis 中, 并通过 Kafka 把消息传递到 MySQL 中
 * 为了一致性安全性使用 Kafka, 而不是直接使用 SpringBoot 中的异步处理
 */
@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    private final CouponDao couponDao;
    private final IRedisService redisService;
    private final TemplateClient templateClient;
    private final SettlementClient settlementClient;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public UserServiceImpl(CouponDao couponDao, IRedisService redisService,
                           TemplateClient templateClient,
                           SettlementClient settlementClient,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.couponDao = couponDao;
        this.redisService = redisService;
        this.templateClient = templateClient;
        this.settlementClient = settlementClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * <h2>根据用户 id 和状态查询优惠券记录</h2>
     *
     * @param userId 用户 id
     * @param status 优惠券状态
     * @return {@link Coupon}s
     */
    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException {
        List<Coupon> curCached = redisService.getCachedCoupons(userId, status);
        List<Coupon> preTarget;

        if (CollectionUtils.isNotEmpty(curCached)) {
            log.debug("coupon cache is not empty: {}. {}", userId, status);
            preTarget = curCached;
        } else {
            log.debug("coupon cache is empty, get coupon from db: {},{}", userId, status);

            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus(userId, CouponStatus.of(status));

            //如果数据库中没有记录,直接返回空列表就可以,Cache中已经加入了一张id为-1的无效优惠券
            if (CollectionUtils.isEmpty(dbCoupons)) {
                log.debug("current user do not have coupon: {},{}", userId, status);
                return dbCoupons;
            }
            //在数据库中找到了记录
            //返回前填充dbCoupons的templateSDK字段, 因为此字段不是数据表的字段
            Map<Integer, CouponTemplateSDK> id2TemplateSDK = templateClient.findIds2TemplateSDK(
                    dbCoupons.stream().map(Coupon::getTemplateId).collect(Collectors.toList())
            ).getData();
            dbCoupons.forEach(dc -> dc.setTemplateSDK(id2TemplateSDK.get(dc.getTemplateId())));

            preTarget = dbCoupons;
            //将记录存入Cache
            redisService.addCouponToCache(userId, preTarget, status);
        }

        //将id为-1的无效优惠券剔除
        preTarget = preTarget.stream().filter(c -> c.getId() != -1).collect(Collectors.toList());

        //如果用户当前查询的是可用优惠券,还需要对已过期的优惠券进行延迟处理，判断此优惠券是否过期
        if (CouponStatus.of(status) == CouponStatus.USABLE) {
            //对获取的优惠券进行分类
            CouponClassify couponClassify = CouponClassify.classify(preTarget);

            /* 过期优惠券延迟处理逻辑 */
            /* 如果分类后存在已过期的优惠券，需要在此处处理 */
            if (CollectionUtils.isNotEmpty(couponClassify.getExpired())) {
                log.info("Add Expired Coupons To Cache From FindCouponsByStatus:{},{}", userId, status);
                //在Redis中处理过期的优惠券
                List<Coupon> expiredCoupons = couponClassify.getExpired();
                expiredCoupons.forEach(c -> c.setStatus(CouponStatus.EXPIRED));
                redisService.addCouponToCache(userId, expiredCoupons, CouponStatus.EXPIRED.getCode());
                //发送消息到Kafka中做异步处理，将过期的优惠券存入DB
                kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(new CouponKafkaMessage(
                                CouponStatus.EXPIRED.getCode(),
                                couponClassify.getExpired().stream().map(Coupon::getId).collect(Collectors.toList())
                        ))
                );
            }
            return couponClassify.getUsable();
        }

        return preTarget;
    }

    /**
     * <h2>根据用户 id 查找当前可以领取的优惠券模板</h2>
     *
     * @param userId 用户 id
     * @return {@link CouponTemplateSDK}s
     */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {
        long curTime = new Date().getTime();
        List<CouponTemplateSDK> templateSDKS = templateClient.findAllUsableTemplateSDK().getData();

        log.debug("Find All Template(From TemplateClint) Count: {}", templateSDKS.size());

        /* 过滤过期的优惠券模板,因为对过期优惠券模板的处理在Template模块下为定时任务,查询时可能存在未被处理的过期模板 */
        templateSDKS = templateSDKS.stream().filter(t -> t.getRule().getExpiration().getDeadline().getTime() > curTime).collect(Collectors.toList());

        log.info("Find Usable Template Count: {}", templateSDKS.size());

        /* 此处只根据优惠券模板的最大领取数判断有哪些优惠券可领取 */
        //key是TemplateId
        //value中的left是Template limitation, right是优惠券模板
        Map<Integer, Pair<Integer, CouponTemplateSDK>> limit2Template = new HashMap<>(templateSDKS.size());
        //将可领取的优惠券模板按limitation进一步封装
        templateSDKS.forEach(t -> limit2Template.put(t.getId(), Pair.of(t.getRule().getLimitation(), t)));

        List<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());
        //用户现有的可用优惠券
        List<Coupon> userUsableCoupons = findCouponsByStatus(userId, CouponStatus.USABLE.getCode());

        log.debug("Current User Has Usable Coupons: {},{}", userId, userUsableCoupons.size());

        //key 是 TemplateId
        /* 把用户现有的可用优惠券按照模板id分组 */
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream().collect(Collectors.groupingBy(Coupon::getTemplateId));


        //根据Template的Rule判断是否可以领取优惠券模板
        limit2Template.forEach((k, v) -> {
            int limitation = v.getLeft();
            CouponTemplateSDK templateSDK = v.getRight();
            /* 用户现有的某一种优惠券已超过该优惠券最大领取数,不加人可用模板列表直接进入下次循环 */
            if (templateId2Coupons.containsKey(k) && templateId2Coupons.get(k).size() >= limitation) {
                return;
            }
            result.add(templateSDK);
        });
        return result;
    }

    /**
     * <h2>根据优惠券模板 id 查找优惠券模板详情</h2>
     *
     * @param id 优惠券模板id
     * @return {@link TemplateInfo}
     */
    @Override
    public TemplateInfo findId2TemplateInfo(Integer id) throws CouponException {
        Map<Integer, CouponTemplateSDK> id2template = templateClient.findIds2TemplateSDK(Collections.singletonList(id)).getData();
        CouponTemplateSDK couponTemplateSDK =id2template.get(id);
        if (couponTemplateSDK == null) {
            throw new CouponException("Template Is Not Exist: " + id);
        }
        return TemplateInfo.to(couponTemplateSDK);
    }

    /**
     * <h2>用户领取优惠券</h2>
     * 1. 从 TemplateClient 拿到对应的优惠券, 并检查是否过期
     * 2. 根据 limitation 判断用户是否可以领取
     * 3. save to db
     * 4. 填充 CouponTemplateSDK
     * 5. save to cache
     *
     * @param request {@link AcquireTemplateRequest}
     * @return {@link Coupon}
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {
        Map<Integer, CouponTemplateSDK> id2template = templateClient.findIds2TemplateSDK(
                Collections.singletonList(request.getTemplateSDK().getId())
        ).getData();

        //判断优惠券模板是否存在
        if (id2template.size() <= 0) {
            log.error("Can Not Acquire Template From TemplateClient: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Template From TemplateClient");
        }


        // 用户是否可以领取这张优惠券
        /* 此处可用findAvailableTemplate实现 */
        List<Coupon> userUsableCoupons = findCouponsByStatus(
                request.getUserId(), CouponStatus.USABLE.getCode()
        );
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons
                .stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));

        if (templateId2Coupons.containsKey(request.getTemplateSDK().getId())
                && templateId2Coupons.get(request.getTemplateSDK().getId()).size() >=
                id2template.get(request.getTemplateSDK().getId()).getRule().getLimitation()) {
            log.error("Exceed Template Assign Limitation: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Exceed Template Assign Limitation");
        }

        //尝试去获取优惠券码
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(
                request.getTemplateSDK().getId()
        );
        if (StringUtils.isEmpty(couponCode)) {
            log.error("Can Not Acquire Coupon Code: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Coupon Code");
        }
        //构建一张优惠券
        Coupon newCoupon = new Coupon(request.getTemplateSDK().getId(), request.getUserId(),
                couponCode, CouponStatus.USABLE);
        //将此优惠券信息存入数据库
        newCoupon = couponDao.save(newCoupon);

        // 填充 Coupon 对象的 CouponTemplateSDK, 一定要在放入缓存之前去填充,因为数据库不包含此字段
        /* id2template.get 从模板服务中获取优惠券模板信息,因为request中的模板信息可能被伪造不安全*/
        newCoupon.setTemplateSDK(id2template.get(request.getTemplateSDK().getId()));
//        newCoupon.setTemplateSDK(request.getTemplateSDK());
        // 放入缓存中
        redisService.addCouponToCache(
                request.getUserId(),
                Collections.singletonList(newCoupon),
                CouponStatus.USABLE.getCode()
        );

        return newCoupon;

    }

    /**
     * <h2>结算(核销)优惠券</h2>
     * 需要注意, 规则相关处理需要由 Settlement 系统去做, 当前系统仅仅做业务处理过程(校验过程)
     * @param info {@link SettlementInfo}
     * @return {@link SettlementInfo}
     */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {

        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = info.getCouponAndTemplateInfos();
        //当没有传递优惠券时,直接返回商品总价
        if (CollectionUtils.isEmpty(ctInfos)) {
            log.info("Empty Coupons For Settle.");
            double goodsSum = 0.0;

            for (GoodsInfo gi : info.getGoodsInfos()) {
                goodsSum += gi.getPrice() * gi.getCount();
            }
            // 没有优惠券也就不存在优惠券的核销, SettlementInfo 其他的字段不需要修改
            info.setCost(retain2Decimals(goodsSum));
        }

        // 校验传递的优惠券是否是用户自己的，防止前端信息被伪造
        List<Coupon> coupons = findCouponsByStatus(info.getUserId(), CouponStatus.USABLE.getCode());

        Map<Integer, Coupon> id2Coupon = coupons.stream().collect(Collectors.toMap(Coupon::getId, Function.identity()));

        if (MapUtils.isEmpty(id2Coupon) || !CollectionUtils.isSubCollection(
                ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId)
                        .collect(Collectors.toList()), id2Coupon.keySet()
        )) {
            log.info("{}", id2Coupon.keySet());
            log.info("{}", ctInfos.stream()
                    .map(SettlementInfo.CouponAndTemplateInfo::getId)
                    .collect(Collectors.toList()));
            log.error("User Coupon Has Some Problem, It Is Not SubCollection" +
                    "Of Coupons!");
            throw new CouponException("User Coupon Has Some Problem, " +
                    "It Is Not SubCollection Of Coupons!");
        }
        log.debug("Current Settlement Coupons Is User's: {}", ctInfos.size());

        //使用的优惠券信息
        List<Coupon> settleCoupons = new ArrayList<>(ctInfos.size());
        ctInfos.forEach(ci -> settleCoupons.add(id2Coupon.get(ci.getId())));

        /* 通过结算服务获取结算后的信息 */
        SettlementInfo processInfo = settlementClient.computeRule(info).getData();

        //在结算时使用优惠券
        if (processInfo.getEmploy() && CollectionUtils.isNotEmpty(processInfo.getCouponAndTemplateInfos())) {
            log.info("Settle User Coupon: {}, {}", info.getUserId(),
                    JSON.toJSONString(settleCoupons));
            //更新缓存
            settleCoupons.forEach(c -> c.setStatus(CouponStatus.USED) );
            redisService.addCouponToCache(info.getUserId(), settleCoupons, CouponStatus.USED.getCode());
            //更新db
            kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(new CouponKafkaMessage(
                            CouponStatus.USED.getCode(), settleCoupons.stream().map(Coupon::getId)
                    .collect(Collectors.toList())
                    ))
            );
        }

        return processInfo;
    }

    /**
     * <h2>保留两位小数</h2>
     */
    private double retain2Decimals(double value) {
        // BigDecimal.ROUND_HALF_UP 代表四舍五入
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
