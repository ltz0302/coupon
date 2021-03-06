package com.ltz.coupon.feign.hystrix;

import com.ltz.coupon.feign.TemplateClient;
import com.ltz.coupon.vo.CommonResponse;
import com.ltz.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * <h1>优惠券模板 Feign 接口的熔断降级策略</h1>
 */
@Slf4j
@Component
public class TemplateClientHystrix implements TemplateClient {
    /**
     * <h2>查找所有可用的优惠券模板<h2/>
     **/
    @Override
    public CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplateSDK() {
        log.error("[eureka-client-coupon-template] findAllUsableTemplate " +
                "request error");
        return new CommonResponse<>(-1,
                "[eureka-client-coupon-template] request error",
                Collections.emptyList());
    }

    /**
     * <h2>获取模板ids到CouponTemplateSDK<h2/>
     *
     * @param ids
     */
    @Override
    public CommonResponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(Collection<Integer> ids) {
        log.error("[eureka-client-coupon-template] findIds2TemplateSDK" +
                "request error");

        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-template] request error",
                new HashMap<>()
        );
    }
}
