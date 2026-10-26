package com.meetingroom.controller;

import com.meetingroom.common.Result;
import com.meetingroom.entity.MeetingRoom;
import com.meetingroom.service.MeetingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会议室接口（FR-03）
 */
@RestController
@RequestMapping("/api")
public class MeetingRoomController {

    @Autowired
    private MeetingRoomService meetingRoomService;

    /**
     * 会议室列表，全部参数可选
     * GET /api/rooms?name=A&minCapacity=10&location=A座&equipment=投影
     * 响应：{ code:0, data:[ { id, roomNo, name, location, capacity, status, facilities:[...] } ] }
     */
    @GetMapping("/rooms")
    public Result<List<MeetingRoom>> list(@RequestParam(required = false) String name,
                                          @RequestParam(required = false) Integer minCapacity,
                                          @RequestParam(required = false) String location,
                                          @RequestParam(required = false) String equipment) {
        return Result.success(meetingRoomService.listRooms(name, minCapacity, location, equipment));
    }
}
