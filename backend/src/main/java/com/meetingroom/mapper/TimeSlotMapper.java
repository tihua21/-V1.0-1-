package com.meetingroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meetingroom.entity.TimeSlot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预置时段 Mapper
 */
@Mapper
public interface TimeSlotMapper extends BaseMapper<TimeSlot> {
}
