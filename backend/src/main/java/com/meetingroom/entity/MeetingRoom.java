package com.meetingroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室实体（对应 meeting_room 表，第 8 周定稿）
 * 占用状态不落库，由 booking 推导；facilities 为关联查询出的设备名称列表
 */
@Data
@TableName("meeting_room")
public class MeetingRoom {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会议室编号，如 A101 */
    private String roomNo;

    private String name;

    private String location;

    private Integer capacity;

    /** 状态：1 启用 / 0 停用，停用后不可被新预约 */
    private Integer status;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 设备名称列表（非表字段，由 meeting_room_facility + facility 关联查出） */
    @TableField(exist = false)
    private List<String> facilities;
}
