package com.ltz.coupon.constant;


/**
 * <h1>常用常量定义</h1>
 */
public class Constant {
    //Kafka消息的Topic
    public static final String TOPIC = "user_coupon_op";

    //Redis Key 前缀定义
    public static class RedisPrefix{
        //优惠码key前缀
        public static final String COUPON_TEMPLATE = "coupon_template_code_";
        //用户当前所有可用的优惠券key前缀
        public static final String USER_COUPON_USABLE = "user_coupon_usable_";
        //用户当前所有已用的优惠券key前缀
        public static final String USER_COUPON_USED = "user_coupon_used_";
        //用户当前所有已过期的优惠券key前缀
        public static final String USER_COUPON_EXPIRED = "user_coupon_expired_";

    }
}
