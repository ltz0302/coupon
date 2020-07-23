package com.ltz.coupon.service;

import com.alibaba.fastjson.JSON;
import com.ltz.coupon.constant.CouponStatus;
import com.ltz.coupon.exception.CouponException;
import com.ltz.coupon.vo.AcquireTemplateRequest;
import com.ltz.coupon.vo.CouponTemplateSDK;
import com.ltz.coupon.vo.TemplateRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * <h1>用户服务功能测试用例</h1>
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    /** fake 一个 UserId */
    private Long fakeUserId = 20001L;

    /** fake 一个 TemplateRequest */
    private TemplateRule.Expiration expiration = new TemplateRule.Expiration(1,1,new Date());
    private TemplateRule templateRule = new TemplateRule(expiration,null,2,null,null);
    private CouponTemplateSDK couponTemplateSDK = new CouponTemplateSDK(4,"优惠券模板-1594710369239",
            "http://www.baidu.com","这是一张优惠券模板","001",2,"200120200714",1,templateRule);
    private AcquireTemplateRequest acquireTemplateRequest = new AcquireTemplateRequest(fakeUserId,couponTemplateSDK);



    @Autowired
    private IUserService userService;

    @Test
    public void testFindCouponByStatus() throws CouponException {

        System.out.println(JSON.toJSONString(
                userService.findCouponsByStatus(
                        fakeUserId,
                        CouponStatus.USABLE.getCode()
                )
        ));
        System.out.println(JSON.toJSONString(
                userService.findCouponsByStatus(
                        fakeUserId,
                        CouponStatus.USED.getCode()
                )
        ));
        System.out.println(JSON.toJSONString(
                userService.findCouponsByStatus(
                        fakeUserId,
                        CouponStatus.EXPIRED.getCode()
                )
        ));
    }

    @Test
    public void testFindAvailableTemplate() throws CouponException {

        System.out.println(JSON.toJSONString(
                userService.findAvailableTemplate(fakeUserId)
        ));
    }


    //测试领取优惠券时需要更改acquireTemplate中对template模块的调用
    @Test
    public void testAcquireTemplate() throws CouponException{
        System.out.println(JSON.toJSONString(
                userService.acquireTemplate(acquireTemplateRequest)
        ));
    }

}
