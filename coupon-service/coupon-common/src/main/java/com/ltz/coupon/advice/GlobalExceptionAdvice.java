package com.ltz.coupon.advice;

import com.ltz.coupon.exception.CouponException;
import com.ltz.coupon.vo.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionAdvice {
    //对CouponException进行统一处理
    @ExceptionHandler(value = CouponException.class)
    public CommonResponse<String> handlerCouponException(HttpServletRequest req, CouponException ex){
        CommonResponse<String> response = new CommonResponse<>(-1,"business error");
        response.setData(ex.toString());
        return response;
    }
}
