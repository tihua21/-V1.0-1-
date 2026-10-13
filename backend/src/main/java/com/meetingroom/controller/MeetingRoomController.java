package com.meetingroom.controller;

import com.meetingroom.common.Result;
import com.meetingroom.entity.MeetingRoom;
import com.meetingroom.service.MeetingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会议室接口
 */
@RestController
@RequestMapping("/api")
public class MeetingRoomController {

    @Autowired
    private MeetingRoomService meetingRoomService;

    /**
     * 查询会议室列表
     * 请求：GET /api/rooms
     * 响应：{ "code": 0, "msg": "success", "data": [ { ... }, { ... } ] }
     */
    @GetMapping("/rooms")
    public Result<List<MeetingRoom>> getAllRooms() {
        List<MeetingRoom> rooms = meetingRoomService.getAllRooms();
        return Result.success(rooms);
    }
}
