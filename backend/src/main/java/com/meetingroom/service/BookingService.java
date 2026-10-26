package com.meetingroom.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.meetingroom.common.Result;
import com.meetingroom.entity.Booking;
import com.meetingroom.entity.BookingAuditLog;
import com.meetingroom.entity.MeetingRoom;
import com.meetingroom.entity.TimeSlot;
import com.meetingroom.mapper.BookingAuditLogMapper;
import com.meetingroom.mapper.BookingMapper;
import com.meetingroom.mapper.MeetingRoomMapper;
import com.meetingroom.mapper.TimeSlotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预约服务
 * 第 9 周范围：提交预约（落库+写日志）、我的预约查询
 * 冲突检测为第 10 周内容，本周不做
 */
@Service
public class BookingService {

    @Autowired
    private BookingMapper bookingMapper;
    @Autowired
    private BookingAuditLogMapper auditLogMapper;
    @Autowired
    private MeetingRoomMapper meetingRoomMapper;
    @Autowired
    private TimeSlotMapper timeSlotMapper;

    /**
     * 提交预约（FR-04）
     * 把预置时段换算成 booking_date + 起止时刻快照保存，状态 0 待审核
     */
    @Transactional
    public Result<Map<String, Object>> create(Long userId, Long roomId, LocalDate date,
                                              Long timeSlotId, String purpose) {
        // 参数完整性由 Controller 校验，这里做合法性校验
        MeetingRoom room = meetingRoomMapper.selectById(roomId);
        if (room == null || room.getStatus() == null || room.getStatus() != 1) {
            return Result.error(409, "该会议室已停用或该时段已被占用");
        }
        TimeSlot slot = timeSlotMapper.selectById(timeSlotId);
        if (slot == null || slot.getStatus() == null || slot.getStatus() != 1) {
            return Result.error(400, "预约信息不完整或时段不合法");
        }

        // 时段模板 → 绝对时刻快照
        LocalDateTime startTime = LocalDateTime.of(date, slot.getStartTime());
        LocalDateTime endTime = LocalDateTime.of(date, slot.getEndTime());

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setRoomId(roomId);
        booking.setSlotId(timeSlotId);
        booking.setBookingDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setPurpose(purpose);
        booking.setStatus(0); // 待审核
        bookingMapper.insert(booking);

        // 审核日志：action=1 提交
        BookingAuditLog log = new BookingAuditLog();
        log.setBookingId(booking.getId());
        log.setOperatorId(userId);
        log.setAction(1);
        log.setFromStatus(null);
        log.setToStatus(0);
        auditLogMapper.insert(log);

        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", booking.getId());
        data.put("status", "待审核");
        return Result.success(data);
    }

    /**
     * 我的预约（FR-06）：只查本人，支持 status / roomId / date 可选筛选
     */
    public List<Booking> listMine(Long userId, Integer status, Long roomId, LocalDate date) {
        QueryWrapper<Booking> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (roomId != null) {
            wrapper.eq("room_id", roomId);
        }
        if (date != null) {
            wrapper.eq("booking_date", date);
        }
        wrapper.orderByDesc("booking_date").orderByDesc("start_time");
        List<Booking> bookings = bookingMapper.selectList(wrapper);

        // 关联填充会议室名称与时段名称，便于前端展示
        Map<Long, String> roomNames = meetingRoomMapper.selectList(null).stream()
                .collect(Collectors.toMap(MeetingRoom::getId, MeetingRoom::getName));
        Map<Long, String> slotNames = timeSlotMapper.selectList(null).stream()
                .collect(Collectors.toMap(TimeSlot::getId, TimeSlot::getSlotName));
        for (Booking b : bookings) {
            b.setRoomName(roomNames.get(b.getRoomId()));
            b.setSlotName(slotNames.get(b.getSlotId()));
        }
        return bookings;
    }
}
