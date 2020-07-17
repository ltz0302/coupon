package com.ltz.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <h1>规则类型枚举定义</h1>
 */
@Getter
@AllArgsConstructor
public enum  RuleFlag {
    // 单类别优惠券定义
    MANJIAN("满减劵的计数规则"),
    ZHEKOU("折扣券的计算规则"),
    LIJIAN("立减券的计算规则"),

    // 多类别优惠券定义
    MANJIAN_ZHEKOU("满减券+折扣券计算规则"),
    MANJIAN_LIJIAN("满减券+立减券计算规则");

    // TODO 更多优惠券类别的组合

    private String description;

}
