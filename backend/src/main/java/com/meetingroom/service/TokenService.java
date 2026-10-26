package com.meetingroom.service;

import com.meetingroom.entity.Role;
import com.meetingroom.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录凭证服务
 * V1.0 单机版：token 为随机 UUID，保存在内存 Map 中，重启后需重新登录
 */
@Service
public class TokenService {

    /** token -> 登录用户信息 */
    private final Map<String, LoginUser> tokenStore = new ConcurrentHashMap<>();

    public String createToken(User user, Role role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, new LoginUser(user.getId(), user.getUsername(),
                user.getRealName(), role.getRoleCode()));
        return token;
    }

    /** 校验并解析 token，无效返回 null */
    public LoginUser parse(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 兼容 "Bearer xxx" 写法
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return tokenStore.get(token);
    }

    /** 登录用户信息（随请求传递） */
    @Data
    @AllArgsConstructor
    public static class LoginUser {
        private Long userId;
        private String username;
        private String realName;
        /** 角色编码：USER / ADMIN */
        private String roleCode;
    }
}
