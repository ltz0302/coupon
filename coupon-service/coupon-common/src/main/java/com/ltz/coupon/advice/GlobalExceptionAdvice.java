package com.ltz.coupon.advice;

import com.ltz.coupon.exception.CouponException;
import com.ltz.coupon.vo.CommonResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionAdvice {
    //只对CouponException进行统一处理
    @ExceptionHandler(value = CouponException.class)
    public CommonResponse<String> handlerCouponException(HttpServletRequest req, CouponException ex){
        CommonResponse<String> response = new CommonResponse<>(-1,"business error");
        response.setData(ex.toString());
        return response;
    }

    //处理Post请求的参数错误异常
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public CommonResponse<String> handlerException(HttpServletRequest req, HttpMessageNotReadableException ex){
        CommonResponse<String> response = new CommonResponse<>(-1,"cannot deserialize");
        response.setData(ex.toString());
        return response;
    }

}
