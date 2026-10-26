package com.meetingroom.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.meetingroom.common.Result;
import com.meetingroom.entity.Role;
import com.meetingroom.entity.User;
import com.meetingroom.mapper.RoleMapper;
import com.meetingroom.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证服务：登录校验（BCrypt）、角色菜单
 */
@Service
public class AuthService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private TokenService tokenService;

    /**
     * 登录
     * @return 成功返回 {token, role, menus}；失败返回 Result 错误（401 账号或密码错误 / 403 账号已停用）
     */
    public Result<Map<String, Object>> login(String username, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return Result.error(401, "账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            return Result.error(403, "账号已停用，请联系管理员");
        }
        Role role = roleMapper.selectById(user.getRoleId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", tokenService.createToken(user, role));
        data.put("role", role.getRoleCode());
        data.put("realName", user.getRealName());
        data.put("menus", menusOf(role.getRoleCode()));
        return Result.success(data);
    }

    /**
     * 按角色返回菜单：普通用户看不到管理员入口
     */
    private List<Map<String, String>> menusOf(String roleCode) {
        List<Map<String, String>> menus = new java.util.ArrayList<>();
        menus.add(Map.of("name", "会议室查询", "path", "/rooms"));
        menus.add(Map.of("name", "我的预约", "path", "/bookings/mine"));
        if ("ADMIN".equals(roleCode)) {
            menus.add(Map.of("name", "预约审核", "path", "/admin/audit"));
            menus.add(Map.of("name", "会议室管理", "path", "/admin/rooms"));
            menus.add(Map.of("name", "用户管理", "path", "/admin/users"));
        }
        return menus;
    }
}
