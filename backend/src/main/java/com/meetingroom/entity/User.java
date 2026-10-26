package com.meetingroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体（对应 user 表，第 8 周定稿）
 * password_hash 只存 BCrypt 哈希，任何接口都不返回该字段
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号，全库唯一 */
    private String username;

    /** BCrypt 哈希，严禁明文 */
    private String passwordHash;

    private String realName;

    /** 性别：0 未知 / 1 男 / 2 女 */
    private Integer gender;

    private String phone;

    private String email;

    /** 所属角色：1 USER / 2 ADMIN */
    private Long roleId;

    /** 账号状态：1 启用 / 0 停用 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
