package com.meetingroom;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智能会议室管理系统 - 后端启动类
 */
@SpringBootApplication
@MapperScan("com.meetingroom.mapper")
public class MeetingRoomApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetingRoomApplication.class, args);
    }
}
