package com.ltz.coupon.feign;

import com.ltz.coupon.feign.hystrix.TemplateClientHystrix;
import com.ltz.coupon.vo.CommonResponse;
import com.ltz.coupon.vo.CouponTemplateSDK;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <h1>优惠券模板微服务 Feign 接口定义</h1>
 */
@FeignClient(value = "eureka-client-coupon-template",fallback = TemplateClientHystrix.class)
public interface TemplateClient {

    /**
     * <h2>查找所有可用的优惠券模板<h2/>
     **/
    @RequestMapping(value = "/coupon-template/template/sdk/all", method = RequestMethod.GET)
    CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplateSDK();

    /**
     * <h2>获取模板ids到CouponTemplateSDK<h2/>
     */
    @RequestMapping(value = "coupon-template/template/sdk/infos",method = RequestMethod.GET)
    CommonResponse<Map<Integer,CouponTemplateSDK>> findIds2TemplateSDK(@RequestParam("ids") Collection<Integer> ids);


}
