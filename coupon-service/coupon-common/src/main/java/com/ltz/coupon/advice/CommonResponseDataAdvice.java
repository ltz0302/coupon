package com.ltz.coupon.advice;

import com.ltz.coupon.annotation.IgnoreResponseAdvice;
import com.ltz.coupon.vo.CommonResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

//根据是否有注解判断是否需要响应处理
@RestControllerAdvice
public class CommonResponseDataAdvice implements ResponseBodyAdvice<Object> {

    //判断是否需要对响应进行处理
    @Override
    @SuppressWarnings("all")
    public boolean supports(MethodParameter methodParameter,
                            Class<? extends HttpMessageConverter<?>> aClass){
        //如果当前方法所在的类有@IgnoreResponseAdvice 注解, 不需要进行通用响应处理
        if(methodParameter.getDeclaringClass().isAnnotationPresent(IgnoreResponseAdvice.class)){
            return false;
        }

        //如果当前方法有@IgnoreResponseAdvice 注解, 不需要进行通用响应处理
        if(methodParameter.getMethod().isAnnotationPresent(IgnoreResponseAdvice.class)){
            return false;
        }
        return true;
    }

    //在返回响应前进行处理
    @Override
    @SuppressWarnings("all")
    public Object beforeBodyWrite(Object object, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        //定义默认的最终返回对象
        CommonResponse<Object> response = new CommonResponse<>(0,"");

        //如果响应的object为空，不需要设置data，返回默认的response
        if(null == object){
            return response;
        }
        // 如果已经是 CommonResponse, 不需要再次处理
        else if(object instanceof CommonResponse){
            response = (CommonResponse<Object>) object;
        }
        // 否则, 把响应对象作为 CommonResponse 的 data 部分
        else {
            response.setData(object);
        }
        return response;
    }
}
