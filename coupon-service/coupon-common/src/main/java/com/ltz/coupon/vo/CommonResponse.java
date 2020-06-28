package com.ltz.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


//通用响应形式定义，利于日志分析
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T>  {
    private Integer code;
    private String message;
    private T data;

    public CommonResponse(Integer code,String message){
        this.code = code;
        this.message = message;
    }
}
