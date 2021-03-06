package com.ltz.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.ltz.coupon.exception.CouponException;
import com.ltz.coupon.executer.ExecuteManager;
import com.ltz.coupon.vo.SettlementInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>结算服务 Controller</h1>
 */
@Slf4j
@RestController
@Api(tags = "SettlementController", description = "优惠券分发模块")
public class SettlementController {

    /** 结算规则执行管理器 */
    private final ExecuteManager executeManager;

    @Autowired
    public SettlementController(ExecuteManager executeManager) {
        this.executeManager = executeManager;
    }

    /**
     * <h2>优惠券结算</h2>
     * 127.0.0.1:7003/coupon-settlement/settlement/compute
     * 127.0.0.1:9000/ltz/coupon-settlement/settlement/compute
     * */
    @PostMapping("/settlement/compute")
    @ApiOperation("优惠券结算")
    public SettlementInfo computeRule(@RequestBody SettlementInfo settlement)
            throws CouponException {
        log.info("settlement: {}", JSON.toJSONString(settlement));
        return executeManager.computeRule(settlement);
    }
}
