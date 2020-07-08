package com.ltz.coupon.vo;

import com.ltz.coupon.constant.CouponStatus;
import com.ltz.coupon.constant.PeriodType;
import com.ltz.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <h1>用户优惠券的分类, 根据优惠券状态</h1>
 * 优惠券的过期为延迟处理，在用户查看优惠券时判断
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponClassify {
    private List<Coupon> usable;
    private List<Coupon> used;
    private List<Coupon> expired;

    /**
     * <h2>对当前的优惠券进行分类<h2/>
     */
    public static CouponClassify classify(List<Coupon> coupons){
        List<Coupon> usable = new ArrayList<>(coupons.size());
        List<Coupon> used = new ArrayList<>(coupons.size());
        List<Coupon> expired = new ArrayList<>(coupons.size());

        coupons.forEach(c->{
            //判断优惠券是否过期
            boolean isTimeExpire;
            long curTime = new Date().getTime();
            //固定时间过期
            if(c.getTemplateSDK().getRule().getExpiration().getPeriod().equals(
                    PeriodType.REGULAR.getCode()
            )){
                isTimeExpire = c.getTemplateSDK().getRule().getExpiration().getDeadline()<=curTime;
            }
            //过期时间和领取时间有关
            else {
                isTimeExpire = DateUtils.addDays(c.getAssignTime(),c.getTemplateSDK().getRule()
                        .getExpiration().getGap()).getTime()<=curTime;
            }
            if(c.getStatus() == CouponStatus.USED){
                used.add(c);
            }
            else if(c.getStatus() == CouponStatus.EXPIRED || isTimeExpire){
                expired.add(c);
            }
            else {
                usable.add(c);
            }
        });
        return new CouponClassify(usable,used,expired);
    }
}
