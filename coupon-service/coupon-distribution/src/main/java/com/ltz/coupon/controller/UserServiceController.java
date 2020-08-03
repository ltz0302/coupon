package com.ltz.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.ltz.coupon.entity.Coupon;
import com.ltz.coupon.exception.CouponException;
import com.ltz.coupon.service.IUserService;
import com.ltz.coupon.vo.AcquireTemplateRequest;
import com.ltz.coupon.vo.CouponTemplateSDK;
import com.ltz.coupon.vo.SettlementInfo;
import com.ltz.coupon.vo.TemplateInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ltz.coupon.feign.TemplateClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <h1>用户服务 Controller</h1>
 */
@Slf4j
@RestController
@Api(tags = "UserServiceController", description = "优惠券分发模块")
public class UserServiceController {

    private final IUserService userService;
    private final TemplateClient templateClient;
    @Autowired
    public UserServiceController(IUserService userService, TemplateClient templateClient) {
        this.userService = userService;
        this.templateClient = templateClient;
    }

    /**
     * <h2>根据用户 id 和优惠券状态查找用户优惠券记录</h2>
     * 127.0.0.1:9000/ltz/coupon-distribution/coupons?userId=1&status=1
     * */
    @GetMapping("/coupons")
    @ApiOperation("根据状态查找用户优惠券记录")
    public List<Coupon> findCouponsByStatus(
            @RequestParam("userId") Long userId,
            @RequestParam("status") Integer status) throws CouponException {

        log.info("Find Coupons By Status: {}, {}", userId, status);
        return userService.findCouponsByStatus(userId, status);
    }

    /**
     * <h2>根据用户 id 查找当前可以领取的优惠券模板</h2>
     * 127.0.0.1:9000/ltz/coupon-distribution/template?userId=1
     * */
    @GetMapping("/template")
    @ApiOperation("查找用户可以领取的优惠券")
    public List<CouponTemplateSDK> findAvailableTemplate(
            @RequestParam("userId") Long userId) throws CouponException {

        log.info("Find Available Template: {}", userId);
        return userService.findAvailableTemplate(userId);
    }


    /**
     * <h2>查看优惠券模板详情</h2>
     * 127.0.0.1:9000/ltz/coupon-distribution/template/info
     * */
    @GetMapping("/template/info")
    @ApiOperation("优惠券模板详情")
    public TemplateInfo findId2TemplateInfo(@RequestParam("userId") Long userId, @RequestParam("id") Integer id)
            throws CouponException {

        log.info("user view template info: {} -> {}", userId, id);
        return userService.findId2TemplateInfo(id);
    }

    /**
     * <h2>用户领取优惠券</h2>
     * 127.0.0.1:9000/ltz/coupon-distribution/acquire/template
     * */
    /* 废弃，现在使用GET方式在优惠券模板详情页直接请求优惠券 */
    @PostMapping("/acquire/template")
    @ApiOperation("领取优惠券")
    public Coupon acquireTemplate(@RequestBody AcquireTemplateRequest request)
            throws CouponException {

        log.info("Acquire Template: {}", JSON.toJSONString(request));
        return userService.acquireTemplate(request);
    }

    /**
     * <h2>用户领取优惠券</h2>
     * 127.0.0.1:9000/ltz/coupon-distribution/acquire/template
     * */
    @GetMapping("/acquire/template")
    @ApiOperation("领取优惠券")
    public Coupon acquireTemplate(@RequestParam("userId") Long userId, @RequestParam Integer id)
            throws CouponException {
        log.info("user {} acquire template {}.", userId, id);

        Map<Integer, CouponTemplateSDK> id2Template = templateClient.findIds2TemplateSDK(
                Collections.singletonList(id)
        ).getData();
        if (MapUtils.isNotEmpty(id2Template)) {
            return userService.acquireTemplate(
                    new AcquireTemplateRequest(userId, id2Template.get(id))
            );
        }
        else throw new CouponException("请求错误,优惠券模板不存在");
    }

    /**
     * <h2>结算(核销)优惠券</h2>
     * 127.0.0.1:9000/ltz/coupon-distribution/settlement
     * */
    @PostMapping("/settlement")
    @ApiOperation("结算优惠券")
    public SettlementInfo settlement(@RequestBody SettlementInfo info)
            throws CouponException {

        log.info("Settlement: {}", JSON.toJSONString(info));
        return userService.settlement(info);
    }
}
