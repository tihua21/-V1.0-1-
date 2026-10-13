package com.meetingroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meetingroom.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper（MyBatis-Plus 自动提供 CRUD 方法）
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
