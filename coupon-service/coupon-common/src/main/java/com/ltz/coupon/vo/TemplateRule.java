package com.ltz.coupon.vo;

import com.ltz.coupon.constant.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

//优惠券规则定义,定义在common模块主要是供CouponTemplateSDK使用
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRule {

    //优惠券过期规则
    //静态内部类是因为内部类需要之后被json序列化
    private Expiration expiration;
    //折扣
    private Discount discount;
    //最多领几张
    private Integer limitation;
    //使用范围: 地域+商品类型
    private Usage usage;
    //权重(可以和哪些优惠券叠加使用, 同一类的优惠券一定不能叠加): list[], 优惠券的唯一编码
    private String weight;

    //校验功能
    public boolean validate(){
        return expiration.validate()&&discount.validate()
                &&limitation>0 && usage.validate()
                && StringUtils.isNotEmpty(weight);
    }

    //有效期规则
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Expiration{

        //有效期规则,对应PeriodType的code字段
        private Integer period;

        //有效间隔:只对变动性有效期有效
        private Integer gap;

        //优惠券失效日期,两类规则都有效
//        private Long deadline;
        private Date deadline;

        boolean validate(){
            //最简化校验
            return null != PeriodType.of(period) && gap >0 &&deadline.getTime() > 0;
        }
    }

    //折扣
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Discount{
        //额度
        private Integer quota;
        //基准，满多少才能用优惠券
        private Integer base;

        boolean validate(){
            return quota > 0 && base>0;
        }
    }

    //使用范围
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage{
        private String province;
        private String city;
        //商品类型
        private String goodsType;
        boolean validate(){
            return StringUtils.isNotEmpty(province)
                    && StringUtils.isNotEmpty(city)
                    && StringUtils.isNotEmpty(goodsType);
        }
    }
}
