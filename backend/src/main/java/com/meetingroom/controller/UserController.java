package com.meetingroom.controller;

import com.meetingroom.common.Result;
import com.meetingroom.entity.User;
import com.meetingroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 登录接口
     * 请求：POST /api/login  body: { "username": "admin", "password": "123456" }
     * 响应：{ "code": 0, "msg": "success", "data": { ... } }
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return Result.error("用户名和密码不能为空");
        }

        User user = userService.login(username, password);
        if (user != null) {
            // 不返回密码
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("用户名或密码错误");
    }
}
