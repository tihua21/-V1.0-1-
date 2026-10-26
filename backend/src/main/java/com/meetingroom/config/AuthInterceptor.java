package com.meetingroom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetingroom.common.Result;
import com.meetingroom.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 登录拦截器
 * 校验 Authorization 请求头中的 token，无效返回 401 未登录或登录已失效；
 * 有效则把登录用户信息放入请求属性，供 Controller 取用
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /** 请求属性名：登录用户信息 */
    public static final String ATTR_LOGIN_USER = "LOGIN_USER";

    @Autowired
    private TokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        TokenService.LoginUser loginUser = tokenService.parse(request.getHeader("Authorization"));
        if (loginUser == null) {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.error(401, "未登录或登录已失效")));
            return false;
        }
        request.setAttribute(ATTR_LOGIN_USER, loginUser);
        return true;
    }
}
