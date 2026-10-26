package com.meetingroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meetingroom.entity.BookingAuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审核日志 Mapper（只增不改不删）
 */
@Mapper
public interface BookingAuditLogMapper extends BaseMapper<BookingAuditLog> {
}
