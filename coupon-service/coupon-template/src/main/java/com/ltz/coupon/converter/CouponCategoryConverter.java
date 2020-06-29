package com.ltz.coupon.converter;

import com.ltz.coupon.constant.CouponCategory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * <h1>优惠券分类枚举属性转换器</h1>
 * AttributeConverter<X, Y>
 * X: 是实体属性的类型
 * Y: 是数据库字段的类型
 */
@Converter
public class CouponCategoryConverter implements AttributeConverter<CouponCategory,String> {
    //将实体属性X转换为Y存储到数据库中, 插入和更新时执行的动作
    @Override
    public String convertToDatabaseColumn(CouponCategory couponCategory) {
        return couponCategory.getCode();
    }

    //将数据库中的字段Y转换为实体属性X, 查询操作时执行的动作
    @Override
    public CouponCategory convertToEntityAttribute(String code) {
        return CouponCategory.of(code);
    }
}
