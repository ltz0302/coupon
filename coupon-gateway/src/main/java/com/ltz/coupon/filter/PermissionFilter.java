package com.ltz.coupon.filter;

import com.alibaba.fastjson.JSON;
import com.ltz.coupon.permission.PermissionClient;
import com.ltz.coupon.vo.CheckPermissionRequest;
import com.ltz.coupon.vo.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <h1>权限过滤器实现</h1>
 */
@Slf4j
@Component
public class PermissionFilter extends AbsSecurityFilter {

    private final PermissionClient permissionClient;

    @Autowired
    public PermissionFilter(PermissionClient permissionClient) {
        this.permissionClient = permissionClient;
    }

    /**
     * <h2>子 Filter 实现该方法, 填充校验逻辑</h2>
     *
     * @param request
     * @param response
     * @return true: 通过校验; false: 校验未通过
     */
    @Override
    protected Boolean interceptCheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // 执行权限校验的逻辑
        // 从 Header 中获取到 userId
        Long userId = Long.valueOf(request.getHeader("userId"));
        String uri = request.getRequestURI().substring("/ltz".length());
        String httpMethod = request.getMethod();

        return permissionClient.checkPermission(new CheckPermissionRequest(userId,uri,httpMethod));
    }

    @Override
    protected int getHttpStatus() {
        return HttpStatus.OK.value();
    }

    @Override
    protected String getErrorMsg() {

        CommonResponse<Object> response = new CommonResponse<>();
        response.setCode(-2);
        response.setMessage("没有权限");

        return JSON.toJSONString(response);
    }
}
