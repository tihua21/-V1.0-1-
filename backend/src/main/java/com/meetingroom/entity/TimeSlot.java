package com.meetingroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预置时段实体（对应 time_slot 表）
 * 每日重复的时刻模板，普通用户只能从中选择，不能自定义起止时间
 */
@Data
@TableName("time_slot")
public class TimeSlot {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 时段名称，如"上午第 1 时段" */
    private String slotName;

    /** 时段开始时刻，如 08:00 */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    /** 时段结束时刻 */
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    /** 时长（分钟） */
    private Integer durationMinutes;

    /** 状态：1 启用 / 0 停用，停用后不可被选择 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
