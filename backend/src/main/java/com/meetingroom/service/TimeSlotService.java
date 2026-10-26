package com.meetingroom.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.meetingroom.entity.TimeSlot;
import com.meetingroom.mapper.TimeSlotMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 预置时段服务：预约表单的可选时段来源
 */
@Service
public class TimeSlotService {

    @Autowired
    private TimeSlotMapper timeSlotMapper;

    /** 启用中的时段，按开始时刻排序 */
    public List<TimeSlot> listEnabled() {
        return timeSlotMapper.selectList(new QueryWrapper<TimeSlot>()
                .eq("status", 1).orderByAsc("start_time"));
    }
}
