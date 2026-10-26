package com.meetingroom.controller;

import com.meetingroom.common.Result;
import com.meetingroom.entity.TimeSlot;
import com.meetingroom.service.TimeSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预置时段接口：为预约表单提供可选时段（FR-03 / FR-08 的读取部分）
 * 本接口为第 9 周新增的清单外支撑接口，已在《第9周联调与阶段检查记录》变更说明中登记
 */
@RestController
@RequestMapping("/api")
public class TimeSlotController {

    @Autowired
    private TimeSlotService timeSlotService;

    /**
     * 启用中的预置时段列表
     * GET /api/time-slots
     */
    @GetMapping("/time-slots")
    public Result<List<TimeSlot>> list() {
        return Result.success(timeSlotService.listEnabled());
    }
}
