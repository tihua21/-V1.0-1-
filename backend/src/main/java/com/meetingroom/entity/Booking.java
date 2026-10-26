package com.meetingroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约实体（对应 booking 表，全库最核心）
 * 按方案 B 固定时段下单：服务端把 slotId 换算成 booking_date + 起止时刻快照保存
 * status：0 待审核 / 1 已预约 / 2 已驳回 / 3 已取消 / 4 已结束
 */
@Data
@TableName("booking")
public class Booking {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 预约编号，审核通过时生成；待审核阶段为空 */
    private String bookingNo;

    /** 预约人 */
    private Long userId;

    /** 会议室 */
    private Long roomId;

    /** 下单时选择的预置时段 */
    private Long slotId;

    /** 预约日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    /** 开始时刻（提交时快照） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    /** 结束时刻（提交时快照），必须晚于 startTime */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;

    /** 用途（会议主题） */
    private String purpose;

    /** 状态：0 待审核 / 1 已预约 / 2 已驳回 / 3 已取消 / 4 已结束 */
    private Integer status;

    private String rejectReason;

    private String cancelReason;

    private Long rescheduleFromId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 会议室名称（非表字段，关联查询供前端展示） */
    @TableField(exist = false)
    private String roomName;

    /** 时段名称（非表字段，关联查询供前端展示） */
    @TableField(exist = false)
    private String slotName;
}
