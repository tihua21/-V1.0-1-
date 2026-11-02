package com.meetingroom.controller;

import com.meetingroom.common.Result;
import com.meetingroom.config.AuthInterceptor;
import com.meetingroom.entity.Booking;
import com.meetingroom.service.BookingService;
import com.meetingroom.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预约接口（FR-04 / FR-06 / 第10周扩展）
 * 均需登录，token 放 Authorization 请求头
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * 提交预约（FR-04 + 第10周冲突检测）
     * POST /api/bookings  body: { roomId, date:"yyyy-MM-dd", timeSlotId, purpose }
     * 成功：{ code:0, data:{ bookingId, bookingNo, status:"待审核" } }
     * 失败：400 参数不合法 / 409 会议室停用或时段冲突
     */
    @PostMapping
    public ResponseEntity<Result<Map<String, Object>>> create(
            @RequestBody Map<String, Object> params,
            @RequestAttribute(AuthInterceptor.ATTR_LOGIN_USER) TokenService.LoginUser loginUser) {

        Object roomId = params.get("roomId");
        Object date = params.get("date");
        Object timeSlotId = params.get("timeSlotId");
        Object purpose = params.get("purpose");
        if (roomId == null || date == null || timeSlotId == null
                || purpose == null || purpose.toString().trim().isEmpty()) {
            return ResponseEntity.status(400).body(Result.error(400, "预约信息不完整或时段不合法"));
        }

        LocalDate bookingDate;
        try {
            bookingDate = LocalDate.parse(date.toString());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Result.error(400, "预约信息不完整或时段不合法"));
        }

        Result<Map<String, Object>> result = bookingService.create(
                loginUser.getUserId(),
                Long.valueOf(roomId.toString()),
                bookingDate,
                Long.valueOf(timeSlotId.toString()),
                purpose.toString().trim());
        if (result.getCode() != 0) {
            return ResponseEntity.status(result.getCode()).body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 我的预约（FR-06）：只查本人记录
     * GET /api/bookings/mine?status=0&roomId=1&date=2026-11-02 （参数均可选）
     */
    @GetMapping("/mine")
    public Result<List<Booking>> mine(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestAttribute(AuthInterceptor.ATTR_LOGIN_USER) TokenService.LoginUser loginUser) {
        return Result.success(bookingService.listMine(loginUser.getUserId(), status, roomId, date));
    }

    // ==================== 第10周新增 ====================

    /**
     * 修改预约（第10周）
     * PUT /api/bookings/{id}  body: { purpose?, timeSlotId? }  —— 二选一或都给
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<Map<String, Object>>> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params,
            @RequestAttribute(AuthInterceptor.ATTR_LOGIN_USER) TokenService.LoginUser loginUser) {

        String purpose = params.get("purpose") == null ? null : params.get("purpose").toString();
        Long newSlotId = null;
        if (params.get("timeSlotId") != null) {
            newSlotId = Long.valueOf(params.get("timeSlotId").toString());
        }

        Result<Map<String, Object>> result = bookingService.update(loginUser.getUserId(), id, purpose, newSlotId);
        if (result.getCode() != 0) {
            return ResponseEntity.status(result.getCode()).body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 取消预约（第10周 —— 普通用户）
     * DELETE /api/bookings/{id}  body: { reason }  可选
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Map<String, Object>>> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> params,
            @RequestAttribute(AuthInterceptor.ATTR_LOGIN_USER) TokenService.LoginUser loginUser) {

        String reason = (params != null && params.get("reason") != null)
                ? params.get("reason").toString() : null;

        Result<Map<String, Object>> result = bookingService.cancelByUser(loginUser.getUserId(), id, reason);
        if (result.getCode() != 0) {
            return ResponseEntity.status(result.getCode()).body(result);
        }
        return ResponseEntity.ok(result);
    }
}
