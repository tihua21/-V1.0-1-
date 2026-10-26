package com.meetingroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meetingroom.entity.MeetingRoomFacility;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会议室-设备关联 Mapper
 */
@Mapper
public interface MeetingRoomFacilityMapper extends BaseMapper<MeetingRoomFacility> {
}
