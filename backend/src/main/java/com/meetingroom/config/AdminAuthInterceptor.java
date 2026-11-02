package com.meetingroom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetingroom.common.Result;
import com.meetingroom.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员权限拦截器
 * 只对 /api/admin/** 生效：在 AuthInterceptor 已确认 token 有效的基础上，
 * 再校验 roleCode == "ADMIN"；非管理员返回 403
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Object userObj = request.getAttribute(AuthInterceptor.ATTR_LOGIN_USER);
        if (!(userObj instanceof TokenService.LoginUser loginUser)) {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.error(401, "未登录或登录已失效")));
            return false;
        }
        if (!"ADMIN".equals(loginUser.getRoleCode())) {
            response.setStatus(403);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.error(403, "无权访问管理员功能")));
            return false;
        }
        return true;
    }
}
