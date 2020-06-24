package com.ltz.coupon.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

public abstract class AbstractZuulFilter extends ZuulFilter {
    // 用于在过滤器之间传递消息, 数据保存在每个请求的 ThreadLocal 中
    // 扩展了 ConcurrentHashMap
    RequestContext context;

    private final static String NEXT = "next";

    //返回true的才继续执行run方法
    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取context中NEXT的value 若没有则采用默认值
        return (boolean) ctx.getOrDefault(NEXT,true);
    }


    @Override
    public Object run() throws ZuulException {
        context = RequestContext.getCurrentContext();
        return cRun();
    }

    protected abstract Object cRun();

    Object fail(int code, String msg){
        context.set(NEXT,false);
        context.setSendZuulResponse(false);
        context.getResponse().setContentType("text/html;charset=UTF-8");
        context.setResponseStatusCode(code);
        context.setResponseBody(String.format("{\"result\": \"%s!\"}", msg));
        return null;
    }

    Object success() {

        context.set(NEXT, true);

        return null;
    }
}
