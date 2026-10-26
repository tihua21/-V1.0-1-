package com.meetingroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体（对应 role 表）
 * 仅两种角色：USER 普通用户 / ADMIN 管理员，程序以 roleCode 鉴权
 */
@Data
@TableName("role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码：USER / ADMIN */
    private String roleCode;

    /** 角色名称：普通用户 / 管理员 */
    private String roleName;

    private String description;

    private LocalDateTime createdAt;
}
