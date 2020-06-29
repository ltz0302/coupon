package com.ltz.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;


//分发目标
@Getter
@AllArgsConstructor
public enum  DistributeTarget {

    SINGLE("单用户",1),
    MULTI("多用户",2);


    private  String description;

    private Integer code;

    public static DistributeTarget of(Integer code) {

        Objects.requireNonNull(code);

        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + " not exists!"));
    }
}

