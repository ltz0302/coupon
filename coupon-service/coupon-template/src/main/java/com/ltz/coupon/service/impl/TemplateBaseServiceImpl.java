package com.ltz.coupon.service.impl;


import com.ltz.coupon.dao.CouponTemplateDao;
import com.ltz.coupon.entity.CouponTemplate;
import com.ltz.coupon.exception.CouponException;
import com.ltz.coupon.service.ITemplateBaseService;
import com.ltz.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TemplateBaseServiceImpl implements ITemplateBaseService {

    private final CouponTemplateDao templateDao;

    @Autowired
    public TemplateBaseServiceImpl(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    /**
     * <h2>根据优惠券模板 id 获取优惠券模板信息</h2>
     *
     * @param id 模板 id
     * @return {@link CouponTemplate} 优惠券模板实体
     */
    @Override
    public CouponTemplate buildTemplateInfo(Integer id) throws CouponException {
        Optional<CouponTemplate> template = templateDao.findById(id);
        if (!template.isPresent()) {
            throw new CouponException("Template Is Not Exist: " + id);
        }

        return template.get();
    }

    /**
     * <h2>查找所有可用的优惠券模板</h2>
     *
     * @return {@link CouponTemplateSDK}s
     */
    @Override
    public List<CouponTemplateSDK> findAllUsableTemplateSDK() {
        List<CouponTemplate> templates =
                templateDao.findAllByAvailableAndExpired(
                        true, false);

        return templates.stream()
                .map(this::template2TemplateSDK).collect(Collectors.toList());
    }

    /**
     * <h2>查找所有可用的优惠券模板</h2>
     *
     * @return {@link CouponTemplate}
     */
    @Override
    public List<CouponTemplate> findUsableTemplate() {
        return templateDao.findAllByAvailableAndExpired(
                true, false);
    }

    /**
     * <h2>查找所有过期的优惠券模板</h2>
     *
     * @return {@link CouponTemplate}
     */
    @Override
    public List<CouponTemplate> findExpiredTemplate() {
        return templateDao.findAllByExpired(true);
    }

    /**
     * <h2>获取模板 ids 到 CouponTemplateSDK 的映射</h2>
     *
     * @param ids 模板 ids
     * @return Map<key: 模板 id ， value: CouponTemplateSDK>
     */
    @Override
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids) {
        List<CouponTemplate> templates = templateDao.findAllById(ids);

        return templates.stream().map(this::template2TemplateSDK)
                .collect(Collectors.toMap(
                        CouponTemplateSDK::getId, Function.identity()
                ));
    }

    /**
     * <h2>将 CouponTemplate 转换为 CouponTemplateSDK</h2>
     * */
    private CouponTemplateSDK template2TemplateSDK(CouponTemplate template) {

        return new CouponTemplateSDK(
                template.getId(),
                template.getName(),
                template.getLogo(),
                template.getDesc(),
                template.getCategory().getCode(),
                template.getProductLine().getCode(),
                template.getKey(),  // 并不是拼装好的 Template Key
                template.getTarget().getCode(),
                template.getRule()
        );
    }
}
