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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 预约服务 —— 第 10 周扩展版
 * 第 9 周：提交预约（落库+写日志）、我的预约查询
 * 第 10 周：冲突检测（半开区间）、预约修改、预约取消、booking_no 生成
 *           管理员看全部、管理员取消异常预约、占用时段查询
 *           提交时自动完成冲突判定，成功即状态「已预约」(1)，无需再走审核
 *
 * 冲突判定：半开区间 [start, end)
 *   新预约与已有预约冲突 ⟺ new.start < old.end AND new.end > old.start
 *   首尾相接不算冲突：old.end == new.start 时，上式 new.start < old.end 不成立 → 放行 ✓
 *   冲突检测在写库的同一个事务里，避免并发竞态
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

    // 占用中的状态：0 待审核 / 1 已预约（这两种都占时段，冲突检测要同时查）
    // 必须用 List<Integer>，int[] 传给 MyBatis-Plus 的 in() 不会展开
    private static final List<Integer> OCCUPIED_STATUSES = Arrays.asList(0, 1);

    // -------------------------- 普通用户 --------------------------

    /**
     * 提交预约（FR-04 + 第10周冲突检测）
     * 半开区间 [start, end) 冲突判定，写库同一事务内
     */
    @Transactional
    public Result<Map<String, Object>> create(Long userId, Long roomId, LocalDate date,
                                              Long timeSlotId, String purpose) {
        // 参数合法性
        MeetingRoom room = meetingRoomMapper.selectById(roomId);
        if (room == null || room.getStatus() == null || room.getStatus() != 1) {
            return Result.error(409, "该会议室已停用或该时段已被占用");
        }
        TimeSlot slot = timeSlotMapper.selectById(timeSlotId);
        if (slot == null || slot.getStatus() == null || slot.getStatus() != 1) {
            return Result.error(400, "预约信息不完整或时段不合法");
        }

        LocalDateTime startTime = LocalDateTime.of(date, slot.getStartTime());
        LocalDateTime endTime = LocalDateTime.of(date, slot.getEndTime());

        // ====== 第10周新增：冲突检测（半开区间） ======
        String conflictMsg = checkConflict(roomId, startTime, endTime, null);
        if (conflictMsg != null) {
            return Result.error(409, conflictMsg);
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setRoomId(roomId);
        booking.setSlotId(timeSlotId);
        booking.setBookingDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setPurpose(purpose);
        // 第 10 周：冲突判定通过即预约成功，直接进入「已预约」(1)，不再等人工审核
        booking.setStatus(1);
        bookingMapper.insert(booking);

        // 生成预约编号（BK + 年月日 + 6位随机）
        String bookingNo = generateBookingNo(booking.getId());
        booking.setBookingNo(bookingNo);
        bookingMapper.updateById(booking);

        writeAuditLog(booking.getId(), userId, 1, null, 1, null);

        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", booking.getId());
        data.put("bookingNo", bookingNo);
        data.put("status", "已预约");
        return Result.success(data);
    }

    /**
     * 修改预约（第10周新增）
     * 只能改自己的、未开始的、状态为 0 或 1 的预约
     * 可改用途 / 可换时段（换时段会重新做冲突检测）
     */
    @Transactional
    public Result<Map<String, Object>> update(Long userId, Long bookingId,
                                              String purpose, Long newTimeSlotId) {
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            return Result.error(404, "预约不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            return Result.error(403, "无权修改他人预约");
        }
        if (booking.getStatus() != 0 && booking.getStatus() != 1) {
            return Result.error(400, "当前状态不可修改");
        }
        if (!booking.getStartTime().isAfter(LocalDateTime.now())) {
            return Result.error(400, "预约已开始，不可修改");
        }

        LocalDateTime oldStart = booking.getStartTime();
        LocalDateTime oldEnd = booking.getEndTime();
        Long oldSlotId = booking.getSlotId();

        boolean slotChanged = (newTimeSlotId != null && !newTimeSlotId.equals(oldSlotId));

        if (slotChanged) {
            TimeSlot slot = timeSlotMapper.selectById(newTimeSlotId);
            if (slot == null || slot.getStatus() == null || slot.getStatus() != 1) {
                return Result.error(400, "新时段不合法");
            }
            LocalDate date = booking.getBookingDate();
            LocalDateTime newStart = LocalDateTime.of(date, slot.getStartTime());
            LocalDateTime newEnd = LocalDateTime.of(date, slot.getEndTime());

            String conflictMsg = checkConflict(booking.getRoomId(), newStart, newEnd, bookingId);
            if (conflictMsg != null) {
                return Result.error(409, conflictMsg);
            }

            booking.setSlotId(newTimeSlotId);
            booking.setStartTime(newStart);
            booking.setEndTime(newEnd);
        }

        if (purpose != null && !purpose.trim().isEmpty()) {
            booking.setPurpose(purpose.trim());
        }

        bookingMapper.updateById(booking);

        // 改期写 action=5；仅改用途写 action=1（提交/修改）
        BookingAuditLog log = new BookingAuditLog();
        log.setBookingId(booking.getId());
        log.setOperatorId(userId);
        // action=5 改期（时段变了）；action=1 重新提交（仅用途变化）
        log.setAction(slotChanged ? 5 : 1);
        // 改期/改用途都不改变当前状态，避免出现没有审核入口的「待审核」死状态
        Integer currentStatus = booking.getStatus();
        log.setFromStatus(currentStatus);
        log.setToStatus(currentStatus);
        auditLogMapper.insert(log);

        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", booking.getId());
        data.put("status", booking.getStatus() == 0 ? "待审核" : "已预约");
        return Result.success(data);
    }

    /**
     * 取消预约（第10周新增 —— 普通用户）
     * 只能取消自己的、未开始的、状态为 0 或 1 的预约
     */
    @Transactional
    public Result<Map<String, Object>> cancelByUser(Long userId, Long bookingId, String reason) {
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            return Result.error(404, "预约不存在");
        }
        if (!booking.getUserId().equals(userId)) {
            return Result.error(403, "无权取消他人预约");
        }
        if (booking.getStatus() != 0 && booking.getStatus() != 1) {
            return Result.error(400, "当前状态不可取消");
        }
        if (!booking.getStartTime().isAfter(LocalDateTime.now())) {
            return Result.error(400, "预约已开始，不可取消");
        }

        Integer fromStatus = booking.getStatus();
        booking.setStatus(3); // 已取消
        booking.setCancelReason(reason != null && !reason.trim().isEmpty() ? reason.trim() : "用户主动取消");
        bookingMapper.updateById(booking);

        writeAuditLog(booking.getId(), userId, 4, fromStatus, 3, booking.getCancelReason());

        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", booking.getId());
        data.put("status", "已取消");
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
        fillRoomAndSlotNames(bookings);
        return bookings;
    }

    // -------------------------- 管理员 --------------------------

    /**
     * 全部预约列表（第10周新增）
     * 支持按 roomId / date / status 筛选，管理员可看所有
     */
    public List<Booking> listAll(Long roomId, LocalDate date, Integer status) {
        QueryWrapper<Booking> wrapper = new QueryWrapper<>();
        if (roomId != null) {
            wrapper.eq("room_id", roomId);
        }
        if (date != null) {
            wrapper.eq("booking_date", date);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("booking_date").orderByDesc("start_time");
        List<Booking> bookings = bookingMapper.selectList(wrapper);
        fillRoomAndSlotNames(bookings);
        return bookings;
    }

    /**
     * 管理员取消异常预约（第10周新增）
     * 可以取消任意状态为 0 / 1 的预约（如已过期无人使用的）
     * 必须填写取消原因
     */
    @Transactional
    public Result<Map<String, Object>> cancelByAdmin(Long bookingId, Long adminUserId, String reason) {
        Booking booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            return Result.error(404, "预约不存在");
        }
        if (booking.getStatus() != 0 && booking.getStatus() != 1) {
            return Result.error(400, "当前状态不可取消");
        }
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error(400, "必须填写取消原因");
        }

        Integer fromStatus = booking.getStatus();
        booking.setStatus(3);
        booking.setCancelReason(reason.trim());
        bookingMapper.updateById(booking);

        writeAuditLog(booking.getId(), adminUserId, 4, fromStatus, 3, reason.trim());

        Map<String, Object> data = new HashMap<>();
        data.put("bookingId", booking.getId());
        data.put("status", "已取消");
        return Result.success(data);
    }

    // -------------------------- 公共查询 --------------------------

    /**
     * 查某间会议室某天的占用时段（第10周新增，供 RoomDetail 用）
     * 只返回状态为占用中的（0 待审核 / 1 已预约）
     */
    public List<Booking> listOccupied(Long roomId, LocalDate date) {
        QueryWrapper<Booking> wrapper = new QueryWrapper<>();
        wrapper.eq("room_id", roomId);
        wrapper.eq("booking_date", date);
        wrapper.in("status", OCCUPIED_STATUSES);
        wrapper.orderByAsc("start_time");
        return bookingMapper.selectList(wrapper);
    }

    // -------------------------- 内部工具 --------------------------

    /**
     * 半开区间冲突检测
     * @param excludeBookingId  修改场景排除自身；null 时不排除
     * @return 有冲突返回错误消息，无冲突返回 null
     */
    private String checkConflict(Long roomId, LocalDateTime newStart, LocalDateTime newEnd, Long excludeBookingId) {
        QueryWrapper<Booking> wrapper = new QueryWrapper<>();
        wrapper.eq("room_id", roomId);
        wrapper.in("status", OCCUPIED_STATUSES);
        // 半开区间 [newStart, newEnd) 与 [oldStart, oldEnd) 冲突 ⟺ newStart < oldEnd AND newEnd > oldStart
        wrapper.lt("start_time", newEnd);   // oldStart < newEnd （即 newEnd > oldStart）
        wrapper.gt("end_time", newStart);   // oldEnd > newStart （即 newStart < oldEnd）
        if (excludeBookingId != null) {
            wrapper.ne("id", excludeBookingId);
        }
        Long count = bookingMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            return "该时段已被占用，请选择其他时段";
        }
        return null;
    }

    /** 生成预约编号：BK + yyyyMMdd + 6位随机 */
    private String generateBookingNo(Long id) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randPart = String.format("%06d", new Random().nextInt(1000000));
        return "BK" + datePart + randPart;
    }

    /** 写审核日志 */
    private void writeAuditLog(Long bookingId, Long operatorId, int action,
                               Integer fromStatus, int toStatus, String reason) {
        BookingAuditLog log = new BookingAuditLog();
        log.setBookingId(bookingId);
        log.setOperatorId(operatorId);
        log.setAction(action);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setReason(reason);
        auditLogMapper.insert(log);
    }

    /** 填充会议室名称与时段名称 */
    private void fillRoomAndSlotNames(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) return;
        Map<Long, String> roomNames = meetingRoomMapper.selectList(null).stream()
                .collect(Collectors.toMap(MeetingRoom::getId, MeetingRoom::getName));
        Map<Long, String> slotNames = timeSlotMapper.selectList(null).stream()
                .collect(Collectors.toMap(TimeSlot::getId, TimeSlot::getSlotName));
        for (Booking b : bookings) {
            b.setRoomName(roomNames.get(b.getRoomId()));
            b.setSlotName(slotNames.get(b.getSlotId()));
        }
    }
}
