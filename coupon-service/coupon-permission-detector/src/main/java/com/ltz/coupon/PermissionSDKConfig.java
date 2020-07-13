package com.ltz.coupon;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>为了引入 permission-sdk 而添加的配置类</h1>
 */
@Configuration
@EnableFeignClients
public class PermissionSDKConfig {
    //feign通过permission-sdk调用permission-service服务
}
