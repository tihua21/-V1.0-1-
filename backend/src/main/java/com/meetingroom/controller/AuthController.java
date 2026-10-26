package com.meetingroom.controller;

import com.meetingroom.common.Result;
import com.meetingroom.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口（FR-01）
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登录
     * POST /api/login  body: { "username": "user1", "password": "123456" }
     * 成功：{ code:0, data:{ token, role, realName, menus } }
     * 失败：400 参数不完整 / 401 账号或密码错误 / 403 账号已停用，请联系管理员
     */
    @PostMapping("/login")
    public ResponseEntity<Result<Map<String, Object>>> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.status(400).body(Result.error(400, "参数不完整"));
        }
        Result<Map<String, Object>> result = authService.login(username, password);
        if (result.getCode() != 0) {
            return ResponseEntity.status(result.getCode()).body(result);
        }
        return ResponseEntity.ok(result);
    }
}
