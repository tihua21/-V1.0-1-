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
 * 管理员预约接口（第10周新增）
 * 必须是 ADMIN 角色，由 AdminAuthInterceptor 拦截器校验
 */
@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * 全部预约列表（第10周）
     * GET /api/admin/bookings?roomId=&date=&status=  （参数均可选）
     */
    @GetMapping
    public Result<List<Booking>> listAll(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer status) {
        return Result.success(bookingService.listAll(roomId, date, status));
    }

    /**
     * 管理员取消异常预约（第10周）
     * PUT /api/admin/bookings/{id}/cancel  body: { reason }  必须填
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Result<Map<String, Object>>> cancel(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params,
            @RequestAttribute(AuthInterceptor.ATTR_LOGIN_USER) TokenService.LoginUser loginUser) {

        String reason = params.get("reason") == null ? null : params.get("reason").toString().trim();
        if (reason == null || reason.isEmpty()) {
            return ResponseEntity.status(400).body(Result.error(400, "必须填写取消原因"));
        }

        Result<Map<String, Object>> result = bookingService.cancelByAdmin(id, loginUser.getUserId(), reason);
        if (result.getCode() != 0) {
            return ResponseEntity.status(result.getCode()).body(result);
        }
        return ResponseEntity.ok(result);
    }
}
