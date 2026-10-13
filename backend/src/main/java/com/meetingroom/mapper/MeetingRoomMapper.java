package com.meetingroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meetingroom.entity.MeetingRoom;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会议室 Mapper（MyBatis-Plus 自动提供 CRUD 方法）
 */
@Mapper
public interface MeetingRoomMapper extends BaseMapper<MeetingRoom> {
}
