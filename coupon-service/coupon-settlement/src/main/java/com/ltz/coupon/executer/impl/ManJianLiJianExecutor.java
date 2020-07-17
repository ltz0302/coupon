package com.ltz.coupon.executer.impl;


import com.alibaba.fastjson.JSON;
import com.ltz.coupon.constant.CouponCategory;
import com.ltz.coupon.constant.RuleFlag;
import com.ltz.coupon.executer.AbstractExecutor;
import com.ltz.coupon.executer.RuleExecutor;
import com.ltz.coupon.vo.GoodsInfo;
import com.ltz.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>满减 + 立减优惠券结算规则执行器</h1>
 */
@Slf4j
@Component
public class ManJianLiJianExecutor extends AbstractExecutor implements RuleExecutor {


    /**
     * <h2>规则类型标记</h2>
     *
     * @return {@link RuleFlag}
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN_LIJIAN;
    }


    /**
     * <h2>校验商品类型与优惠券是否匹配</h2>
     * 需要注意:
     * 1. 这里实现的单品类优惠券的校验, 多品类优惠券重载此方法
     * 2. 商品只需要有一个优惠券要求的商品类型去匹配就可以
     *
     * @param settlement {@link SettlementInfo} 用户传递的计算信息
     */
    @Override
    @SuppressWarnings("all")
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement) {
        log.debug("Check ManJian And LiJian Is Match Or Not!");
        List<Integer> goodsType = settlement.getGoodsInfos().stream()
                .map(GoodsInfo::getType).collect(Collectors.toList());
        List<Integer> templateGoodsType = new ArrayList<>();

        settlement.getCouponAndTemplateInfos().forEach(ct -> {
            templateGoodsType.addAll(JSON.parseObject(ct.getTemplate()
                    .getRule().getUsage().getGoodsType(), List.class));
        });

        // 如果想要使用多类优惠券, 则必须要所有的商品类型都包含在内, 即差集为空
        return CollectionUtils.isEmpty(CollectionUtils.subtract(
                goodsType, templateGoodsType
        ));
    }



    /**
     * <h2>优惠券规则的计算</h2>
     *
     * @param settlement {@link SettlementInfo} 包含了选择的优惠券
     * @return {@link SettlementInfo} 修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double goodsSum = retain2Decimals(goodsCostSum(settlement.getGoodsInfos()));

        //商品类型校验
        SettlementInfo probability = processGoodsTypeNotSatisfy(settlement, goodsSum);
        if (null != probability) {
            log.debug("ManJian And LiJian Template Is Not Match To GoodsType!");
            return probability;
        }
        SettlementInfo.CouponAndTemplateInfo manJian = null;
        SettlementInfo.CouponAndTemplateInfo lijian = null;

        for (SettlementInfo.CouponAndTemplateInfo ct :
                settlement.getCouponAndTemplateInfos()) {
            if (CouponCategory.of(ct.getTemplate().getCategory()) == CouponCategory.MANJIAN) {
                manJian = ct;
            } else {
                lijian = ct;
            }
        }

        assert null != manJian;
        assert null != lijian;

        // 当前的折扣优惠券和满减优惠券不能一起使用,清空优惠券,返回商品原价
        if(!isTemplateCanShared(manJian,lijian)) {
            log.debug("Current ManJian And LiJian Can Not Shared!");
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        //实际使用的优惠券
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = new ArrayList<>();

        double manJianBase = (double) manJian.getTemplate().getRule().getDiscount().getBase();
        double manJianQuota = (double) manJian.getTemplate().getRule().getDiscount().getQuota();

        //最终的价格
        double targetSum = goodsSum;

        //先计算满减
        if(targetSum >= manJianBase){
            targetSum -= manJianQuota;
            ctInfos.add(manJian);
        }

        // 再计算立减
        double lijianQuota = (double) lijian.getTemplate().getRule().getDiscount().getQuota();
        targetSum -= lijianQuota;
        ctInfos.add(lijian);

        settlement.setCouponAndTemplateInfos(ctInfos);
        settlement.setCost(retain2Decimals(targetSum > minCost() ? targetSum : minCost()));

        log.debug("Use ManJian And LiJian Coupon Make Goods Cost From {} To {}",
                goodsSum, settlement.getCost());

        return settlement;
    }


    /**
     * <h2>当前的两张优惠券是否可以共用</h2>
     * 即校验 TemplateRule 中的 weight 是否满足条件
     */
    @SuppressWarnings("all")
    private boolean
    isTemplateCanShared(SettlementInfo.CouponAndTemplateInfo manJian,
                        SettlementInfo.CouponAndTemplateInfo lijian) {

        String manjianKey = manJian.getTemplate().getKey()
                + String.format("%04d", manJian.getTemplate().getId());
        String lijianKey = lijian.getTemplate().getKey()
                + String.format("%04d", lijian.getTemplate().getId());

        //将满减券本身和在weight中标识的可共用优惠券加入列表
        List<String> allSharedKeysForManjian = new ArrayList<>();
        allSharedKeysForManjian.add(manjianKey);
        allSharedKeysForManjian.addAll(JSON.parseObject(
                manJian.getTemplate().getRule().getWeight(),
                List.class
        ));

        List<String> allSharedKeysForLijian = new ArrayList<>();
        allSharedKeysForLijian.add(lijianKey);
        allSharedKeysForLijian.addAll(JSON.parseObject(
                lijian.getTemplate().getRule().getWeight(),
                List.class
        ));

        return CollectionUtils.isSubCollection(
                Arrays.asList(manjianKey, lijianKey), allSharedKeysForManjian)
                || CollectionUtils.isSubCollection(
                Arrays.asList(manjianKey, lijianKey), allSharedKeysForLijian
        );
    }
}
