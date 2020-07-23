package com.ltz.coupon.controller;

import com.alibaba.fastjson.JSON;
//import com.ltz.coupon.annotation.CouponPermission;
//import com.ltz.coupon.annotation.IgnorePermission;
import com.ltz.coupon.entity.CouponTemplate;
import com.ltz.coupon.exception.CouponException;
import com.ltz.coupon.service.IBuildTemplateService;
import com.ltz.coupon.service.ITemplateBaseService;
import com.ltz.coupon.vo.CouponTemplateSDK;
import com.ltz.coupon.vo.TemplateRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <h1>优惠券模板相关的功能控制器</h1>
 */
@Slf4j
@RestController
@Api(tags = "CouponTemplateController", description = "优惠券模板模块")
public class CouponTemplateController {

    //构建优惠券模板服务
    private final IBuildTemplateService buildTemplateService;

    //优惠券模板基础服务
    private final ITemplateBaseService templateBaseService;

    @Autowired
    public CouponTemplateController(IBuildTemplateService buildTemplateService, ITemplateBaseService templateBaseService) {
        this.buildTemplateService = buildTemplateService;
        this.templateBaseService = templateBaseService;
    }

    /**
     * <h2>构建优惠券模板</h2>
     * 127.0.0.1:7001/coupon-template/template/build
     * 127.0.0.1:9000/ltz/coupon-template/template/build
     */
    @PostMapping("/template/build")
//    @CouponPermission(description = "buildTemplate", readOnly = false)
    @ApiOperation("构建优惠券模板")
    public CouponTemplate buildTemplate(@RequestBody TemplateRequest request) throws CouponException {
        log.info("Build Template: {}", JSON.toJSONString(request));
        return buildTemplateService.buildTemplate(request);
    }

    /**
     * <h2>构造优惠券模板详情</h2>
     * 127.0.0.1:7001/coupon-template/template/info?id=1
     * 127.0.0.1:9000/ltz/coupon-template/template/info?id=1
     */
    @GetMapping("/template/info")
//    @CouponPermission(description = "buildTemplateInfo")
    @ApiOperation("获取优惠券模板详情")
    public CouponTemplate buildTemplateInfo(@RequestParam("id") Integer id) throws CouponException {
        log.info("Build Template Info For: {}", id);
        return templateBaseService.buildTemplateInfo(id);
    }


    /* 下面两个接口供分发服务调用 */

    /**
     * <h2>查找所有可用的优惠券模板</h2>
     * 127.0.0.1:7001/coupon-template/template/sdk/all
     * 127.0.0.1:9000/ltz/coupon-template/template/sdk/all
     */
    @GetMapping("/template/sdk/all")
//    @IgnorePermission
    @ApiOperation("查找所有可用的优惠券模板")
    public List<CouponTemplateSDK> findAllUsableTemplate() {
        log.info("Find All Usable Template.");
        return templateBaseService.findAllUsableTemplate();
    }

    /**
     * <h2>获取模板 ids 到 CouponTemplateSDK 的映射</h2>
     * 127.0.0.1:7001/coupon-template/template/sdk/infos?ids=1,2
     * 127.0.0.1:9000/ltz/coupon-template/template/sdk/infos?ids=1,2
     */
    @GetMapping("template/sdk/infos")
    @ApiOperation("获取优惠券模板的SDK详情")
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(@RequestParam("ids") Collection<Integer> ids) {
        log.info("FindIds2TemplateSDK: {}", JSON.toJSONString(ids));
        return templateBaseService.findIds2TemplateSDK(ids);
    }


}

