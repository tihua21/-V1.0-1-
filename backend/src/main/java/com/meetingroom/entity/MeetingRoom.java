package com.meetingroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会议室实体
 */
@Data
@TableName("meeting_rooms")
public class MeetingRoom {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String location;

    private Integer capacity;

    /** 状态：available / occupied / maintenance */
    private String status;

    private LocalDateTime createdAt;
}
