package com.ltz.coupon.service.impl;


import com.ltz.coupon.dao.CouponTemplateDao;
import com.ltz.coupon.entity.CouponTemplate;
import com.ltz.coupon.exception.CouponException;
import com.ltz.coupon.service.IAsyncService;
import com.ltz.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ltz.coupon.service.IBuildTemplateService;

/**
 * <h1>构建优惠券模板接口实现</h1>
 */
@Slf4j
@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService {
    //异步任务
    private final IAsyncService asyncService;
    //CouponTemplate Dao
    private final CouponTemplateDao templateDao;

    @Autowired
    public BuildTemplateServiceImpl(IAsyncService asyncService, CouponTemplateDao templateDao) {
        this.asyncService = asyncService;
        this.templateDao = templateDao;
    }


    /**
     * <h2>创建优惠券模板</h2>
     *
     * @param request {@link TemplateRequest} 模板信息请求对象
     * @return {@link CouponTemplate} 优惠券模板实体
     */
    @Override
    public CouponTemplate buildTemplate(TemplateRequest request) throws CouponException {
        //参数合法性校验
        if (!request.validate()) {
            throw new CouponException("BuildTemplate Param Is Not Valid!");
        }

        if (null != templateDao.findByName(request.getName())) {
            throw new CouponException("Exist Same Name Template!");
        }

        //构造 CouponTemplate 并保存到数据库中
        CouponTemplate template = requestToTemplate(request);
        template = templateDao.save(template);

        //根据优惠券模板异步生成优惠券码
        asyncService.asyncConstructCouponByTemplate(template);

        return template;
    }

    //将TemplateRequest 转换为 CouponTemplate
    private CouponTemplate requestToTemplate(TemplateRequest request) {

        return new CouponTemplate(
                request.getName(),
                request.getLogo(),
                request.getDesc(),
                request.getCategory(),
                request.getProductLine(),
                request.getCount(),
                request.getUserId(),
                request.getTarget(),
                request.getRule()
        );
    }
}
