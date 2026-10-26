package com.meetingroom.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.meetingroom.entity.Facility;
import com.meetingroom.entity.MeetingRoom;
import com.meetingroom.mapper.FacilityMapper;
import com.meetingroom.mapper.MeetingRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会议室服务：条件筛选 + 设备挂载
 */
@Service
public class MeetingRoomService {

    @Autowired
    private MeetingRoomMapper meetingRoomMapper;
    @Autowired
    private FacilityMapper facilityMapper;
    @Autowired
    private com.meetingroom.mapper.MeetingRoomFacilityMapper meetingRoomFacilityMapper;

    /**
     * 条件查询会议室列表（FR-03）
     * @param name        名称模糊匹配，可选
     * @param minCapacity 最小容量，可选
     * @param location    位置模糊匹配，可选
     * @param equipment   设备名称模糊匹配，可选
     */
    public List<MeetingRoom> listRooms(String name, Integer minCapacity, String location, String equipment) {
        QueryWrapper<MeetingRoom> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like("name", name);
        }
        if (minCapacity != null) {
            wrapper.ge("capacity", minCapacity);
        }
        if (location != null && !location.isEmpty()) {
            wrapper.like("location", location);
        }
        // 设备筛选：先查出匹配设备的会议室 id 集合
        if (equipment != null && !equipment.isEmpty()) {
            List<Long> roomIds = roomIdsByEquipment(equipment);
            if (roomIds.isEmpty()) {
                return Collections.emptyList();
            }
            wrapper.in("id", roomIds);
        }
        wrapper.orderByAsc("id");
        List<MeetingRoom> rooms = meetingRoomMapper.selectList(wrapper);
        fillFacilities(rooms);
        return rooms;
    }

    /** 按设备名称模糊查拥有该设备的会议室 id */
    private List<Long> roomIdsByEquipment(String equipment) {
        List<Facility> facilities = facilityMapper.selectList(
                new QueryWrapper<Facility>().like("name", equipment));
        if (facilities.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> facilityIds = facilities.stream().map(Facility::getId).collect(Collectors.toList());
        return meetingRoomFacilityMapper.selectList(
                        new QueryWrapper<com.meetingroom.entity.MeetingRoomFacility>()
                                .in("facility_id", facilityIds))
                .stream().map(com.meetingroom.entity.MeetingRoomFacility::getRoomId)
                .distinct().collect(Collectors.toList());
    }

    /** 为每间会议室填充设备名称列表 */
    private void fillFacilities(List<MeetingRoom> rooms) {
        if (rooms.isEmpty()) {
            return;
        }
        List<Long> roomIds = rooms.stream().map(MeetingRoom::getId).collect(Collectors.toList());
        List<com.meetingroom.entity.MeetingRoomFacility> relations = meetingRoomFacilityMapper.selectList(
                new QueryWrapper<com.meetingroom.entity.MeetingRoomFacility>().in("room_id", roomIds));
        Map<Long, String> facilityNames = facilityMapper.selectList(null).stream()
                .collect(Collectors.toMap(Facility::getId, Facility::getName));
        for (MeetingRoom room : rooms) {
            room.setFacilities(relations.stream()
                    .filter(r -> r.getRoomId().equals(room.getId()))
                    .map(r -> facilityNames.get(r.getFacilityId()))
                    .collect(Collectors.toList()));
        }
    }
}
