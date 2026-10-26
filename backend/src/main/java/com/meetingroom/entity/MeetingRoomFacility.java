package com.meetingroom.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 会议室-设备关联（对应 meeting_room_facility 表，组合主键 room_id + facility_id）
 */
@Data
@TableName("meeting_room_facility")
public class MeetingRoomFacility {

    private Long roomId;

    private Long facilityId;
}
