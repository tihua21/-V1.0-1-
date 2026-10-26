package com.meetingroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备实体（对应 facility 表）
 */
@Data
@TableName("facility")
public class Facility {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备名称，如投影仪、电子白板 */
    private String name;

    private String remark;

    private LocalDateTime createdAt;
}
