package com.meetingroom.service;

import com.meetingroom.entity.MeetingRoom;
import com.meetingroom.mapper.MeetingRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会议室服务
 */
@Service
public class MeetingRoomService {

    @Autowired
    private MeetingRoomMapper meetingRoomMapper;

    /**
     * 查询所有会议室
     * @return 会议室列表（0 条也算正常）
     */
    public List<MeetingRoom> getAllRooms() {
        return meetingRoomMapper.selectList(null);
    }
}
