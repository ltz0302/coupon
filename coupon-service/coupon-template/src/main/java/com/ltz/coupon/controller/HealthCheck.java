package com.ltz.coupon.controller;


import com.ltz.coupon.exception.CouponException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>健康检查接口</h1>
 */
@Slf4j
@RestController
@Api(tags = "HealthCheck", description = "健康检查")
public class HealthCheck {
    //服务发现客户端
    private final DiscoveryClient client;

    //服务注册接口,提供了获取服务id的方法
    private final Registration registration;

    @Autowired
    public HealthCheck(DiscoveryClient client, Registration registration) {
        this.client = client;
        this.registration = registration;
    }

    /**
     * <h2>健康检查接口</h2>
     * 127.0.0.1:7001/coupon-template/health
     * 127.0.0.1:9000/ltz/coupon-template/health
     */
    @GetMapping("/health")
    @ApiOperation("健康检查")
    public String health() {
        log.debug("view health api");
        return "CouponTemplate Is OK!";
    }

    /**
     * <h2>异常测试接口</h2>
     * 127.0.0.1:7001/coupon-template/exception
     * 127.0.0.1:9000/ltz/coupon-template/exception
     */
    @GetMapping("exception")
    @ApiOperation("异常测试")
    public String exception() throws CouponException {
        log.debug("view exception api");
        throw new CouponException("CouponTemplate Has Some Problem");
    }

    /**
     * <h2>获取 Eureka Server 上的微服务元信息</h2>
     * 127.0.0.1:7001/coupon-template/info
     * 127.0.0.1:9000/ltz/coupon-template/info
     */
    @GetMapping("/info")
    @ApiOperation("获取微服务元信息")
    public List<Map<String, Object>> info() {

        // 大约需要等待两分钟时间才能获取到注册信息
        List<ServiceInstance> instances =
                client.getInstances(registration.getServiceId());
        List<Map<String, Object>> result = new ArrayList<>(instances.size());

        instances.forEach(i -> {
            Map<String, Object> info = new HashMap<>();
            info.put("serviceId", i.getServiceId());
            info.put("instanceId", i.getInstanceId());
            info.put("post", i.getPort());
            result.add(info);
        });
        return result;
    }
}