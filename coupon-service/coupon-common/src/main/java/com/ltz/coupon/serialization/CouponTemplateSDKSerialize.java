package com.ltz.coupon.serialization;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.ltz.coupon.constant.CouponCategory;
import com.ltz.coupon.constant.DistributeTarget;
import com.ltz.coupon.constant.ProductLine;
import com.ltz.coupon.vo.CouponTemplateSDK;

import java.io.IOException;


public class CouponTemplateSDKSerialize extends JsonSerializer<CouponTemplateSDK> {
    @Override
    public void serialize(CouponTemplateSDK couponTemplateSDK, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
        // 开始序列化
        generator.writeStartObject();

        generator.writeStringField("id", couponTemplateSDK.getId().toString());
        generator.writeStringField("name",
                couponTemplateSDK.getName());
        generator.writeStringField("logo",
                couponTemplateSDK.getLogo());
        generator.writeStringField("desc",
                couponTemplateSDK.getDesc());
        generator.writeStringField("category",
                CouponCategory.of(couponTemplateSDK.getCategory()).getDescription());
        generator.writeStringField("productLine",
                ProductLine.of(couponTemplateSDK.getProductLine()).getDescription());
        generator.writeStringField("key",
                couponTemplateSDK.getKey());
        generator.writeStringField("target",
                DistributeTarget.of(couponTemplateSDK.getTarget()).getDescription());
        generator.writeStringField("expiration",
                JSON.toJSONString(
                        couponTemplateSDK.getRule().getExpiration()));
        generator.writeStringField("discount",
                JSON.toJSONString(
                        couponTemplateSDK.getRule().getDiscount()));
        generator.writeStringField("usage",
                JSON.toJSONString(couponTemplateSDK.getRule().getUsage()));

        // 结束序列化
        generator.writeEndObject();
    }
}
