package com.meetingroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核日志实体（对应 booking_audit_log 表）
 * 只增不改不删；action：1 提交 / 2 通过 / 3 驳回 / 4 取消 / 5 改期 / 6 结束
 */
@Data
@TableName("booking_audit_log")
public class BookingAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 被操作的预约 */
    private Long bookingId;

    /** 操作人；系统自动操作时为空 */
    private Long operatorId;

    /** 操作类型：1 提交 / 2 通过 / 3 驳回 / 4 取消 / 5 改期 / 6 结束 */
    private Integer action;

    /** 操作前状态；提交时为 NULL */
    private Integer fromStatus;

    /** 操作后状态 */
    private Integer toStatus;

    private String reason;

    private LocalDateTime createdAt;
}
