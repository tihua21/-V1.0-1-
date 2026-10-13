package com.meetingroom.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.meetingroom.entity.User;
import com.meetingroom.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录验证
     * @param username 用户名
     * @param password 密码
     * @return 验证通过返回 User 对象，否则返回 null
     */
    public User login(String username, String password) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username).eq("password", password);
        return userMapper.selectOne(wrapper);
    }
}
