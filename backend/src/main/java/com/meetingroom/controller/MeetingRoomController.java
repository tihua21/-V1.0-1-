package com.meetingroom.controller;

import com.meetingroom.common.Result;
import com.meetingroom.entity.Booking;
import com.meetingroom.entity.MeetingRoom;
import com.meetingroom.service.BookingService;
import com.meetingroom.service.MeetingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 会议室接口（FR-03 + 第10周扩展）
 */
@RestController
@RequestMapping("/api")
public class MeetingRoomController {

    @Autowired
    private MeetingRoomService meetingRoomService;
    @Autowired
    private BookingService bookingService;

    /**
     * 会议室列表，全部参数可选
     * GET /api/rooms?name=A&minCapacity=10&location=A座&equipment=投影
     */
    @GetMapping("/rooms")
    public Result<List<MeetingRoom>> list(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) Integer minCapacity,
                                          @RequestParam(required = false) String location,
                                          @RequestParam(required = false) String equipment) {
        return Result.success(meetingRoomService.listRooms(name, minCapacity, location, equipment));
    }

    /**
     * 某间会议室某天的占用时段（第10周新增，供 RoomDetail 用）
     * GET /api/rooms/{id}/occupied?date=2026-11-02
     * 只返回状态为占用中的（0 待审核 / 1 已预约）
     */
    @GetMapping("/rooms/{id}/occupied")
    public Result<List<Booking>> occupied(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(bookingService.listOccupied(id, date));
    }
}
