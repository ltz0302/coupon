package com.ltz.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.ltz.coupon.entity.Coupon;
import com.ltz.coupon.exception.CouponException;
import com.ltz.coupon.service.IUserService;
import com.ltz.coupon.vo.AcquireTemplateRequest;
import com.ltz.coupon.vo.CouponTemplateSDK;
import com.ltz.coupon.vo.SettlementInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <h1>用户服务 Controller</h1>
 */
@Slf4j
@RestController
@Api(tags = "UserServiceController", description = "优惠券分发模块")
public class UserServiceController {

    private final IUserService userService;
    @Autowired
    public UserServiceController(IUserService userService) {
        this.userService = userService;
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
     * <h2>用户领取优惠券</h2>
     * 127.0.0.1:9000/ltz/coupon-distribution/acquire/template
     * */
    @PostMapping("/acquire/template")
    @ApiOperation("领取优惠券")
    public Coupon acquireTemplate(@RequestBody AcquireTemplateRequest request)
            throws CouponException {

        log.info("Acquire Template: {}", JSON.toJSONString(request));
        return userService.acquireTemplate(request);
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
