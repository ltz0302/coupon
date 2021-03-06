package com.ltz.coupon.service;


import com.ltz.coupon.entity.Coupon;
import com.ltz.coupon.exception.CouponException;
import com.ltz.coupon.vo.AcquireTemplateRequest;
import com.ltz.coupon.vo.CouponTemplateSDK;
import com.ltz.coupon.vo.SettlementInfo;
import com.ltz.coupon.vo.TemplateInfo;
import io.netty.channel.pool.FixedChannelPool;

import java.util.List;

/**
 * <h1>用户服务相关的接口定义</h1>
 * 1. 用户三类状态优惠券信息展示服务
 * 2. 查看用户当前可以领取的优惠券模板 - coupon-template 微服务配合实现
 * 3. 用户领取优惠券服务
 * 4. 用户消费优惠券服务 - coupon-settlement 微服务配合实现
 */
public interface IUserService {
    /**
     * <h2>根据用户 id 和状态查询优惠券记录</h2>
     * @param userId 用户 id
     * @param status 优惠券状态
     * @return {@link Coupon}
     * */
    List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException;


    /**
     * <h2>根据用户 id 查找当前可以领取的优惠券模板</h2>
     * @param userId 用户 id
     * @return {@link CouponTemplateSDK}
     * */
    List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException;


    /**
     * <h2>根据优惠券模板 id 查找优惠券模板详情</h2>
     * @param id 优惠券模板id
     * @return {@link TemplateInfo}
     * */
    TemplateInfo findId2TemplateInfo(Integer id) throws CouponException;

    /**
     * <h2>用户领取优惠券</h2>
     * @param request {@link AcquireTemplateRequest}
     * @return {@link Coupon}
     * */
    Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException;

    /**
     * <h2>结算(核销)优惠券</h2>
     * @param info {@link SettlementInfo}
     * @return {@link SettlementInfo}
     * */
    SettlementInfo settlement(SettlementInfo info) throws CouponException;
}
