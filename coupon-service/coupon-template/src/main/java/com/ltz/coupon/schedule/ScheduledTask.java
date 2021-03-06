package com.ltz.coupon.schedule;

import com.ltz.coupon.dao.CouponTemplateDao;
import com.ltz.coupon.entity.CouponTemplate;
import com.ltz.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <h1>定时清理已过期的优惠券模板</h1>
 */
@Slf4j
@Component
public class ScheduledTask {

    private final CouponTemplateDao templateDao;

    @Autowired
    public ScheduledTask(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    //更改过期的优惠券模板expired字段为true
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void offlineCouponTemplate() {
        log.info("Start To Expire CouponTemplate");

        List<CouponTemplate> templates = templateDao.findAllByExpired(false);
        if (CollectionUtils.isEmpty(templates)) {
            log.info("Done To Expire CouponTemplate.");
            return;
        }

        Date cur = new Date();
        List<CouponTemplate> expiredTemplates = new ArrayList<>(templates.size());

        templates.forEach(t -> {
            // 根据优惠券模板规则中的 "过期规则" 校验模板是否过期
            TemplateRule rule = t.getRule();
            if (rule.getExpiration().getDeadline().getTime() < cur.getTime()) {
                t.setExpired(true);
                expiredTemplates.add(t);
            }
        });

        //将更改后的模板保存回数据库(更改了expired字段)
        if (CollectionUtils.isNotEmpty(expiredTemplates)) {
            log.info("Expired CouponTemplate Num: {}",
                    templateDao.saveAll(expiredTemplates));
        }
        log.info("Done To Expire CouponTemplate.");
    }

}
