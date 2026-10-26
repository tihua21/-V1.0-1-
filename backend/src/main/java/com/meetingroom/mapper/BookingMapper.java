package com.meetingroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meetingroom.entity.Booking;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约 Mapper
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
}
