-- =====================================================================
-- 智能会议室管理系统 V1.0 —— 建表与初始化脚本
-- 数据库：MySQL 8.4 / 字符集 utf8mb4
-- 使用方法：
--   方式一：mysql -u root -p --default-character-set=utf8mb4
--          source docs/week07-08/schema.sql
--   方式二：mysql -u root -p --default-character-set=utf8mb4 < docs/week07-08/schema.sql
--
-- 本脚本与《数据库与接口设计》物理表结构完全对应，共 8 张表。
-- 密码使用 BCrypt 哈希，脚本中不出现明文，测试口令见 README。
-- 冲突判定采用半开区间 [start, end)，首尾相接不算冲突。
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS meeting_room;
CREATE DATABASE meeting_room DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE meeting_room;

-- =====================================================================
-- 1. role（角色表）
-- 系统仅两种角色，由初始化脚本写入，程序以 role_code 判断权限。
-- =====================================================================
CREATE TABLE role (
    id          BIGINT       NOT NULL AUTO_INCREMENT            COMMENT '角色主键',
    role_code   VARCHAR(20)  NOT NULL                           COMMENT '角色编码：USER / ADMIN，鉴权使用',
    role_name   VARCHAR(50)  NOT NULL                           COMMENT '角色名称：普通用户 / 管理员',
    description VARCHAR(200) DEFAULT NULL                       COMMENT '角色职责说明',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =====================================================================
-- 2. user（用户表）
-- 普通用户与管理员共用一张表，用 role_id 区分。
-- password_hash 只存 BCrypt 哈希（60 字符），不存明文。
-- =====================================================================
CREATE TABLE user (
    id            BIGINT       NOT NULL AUTO_INCREMENT            COMMENT '用户主键',
    username      VARCHAR(50)  NOT NULL                           COMMENT '登录账号，全库唯一',
    password_hash VARCHAR(100) NOT NULL                           COMMENT 'BCrypt 哈希，严禁存明文',
    real_name     VARCHAR(50)  NOT NULL                           COMMENT '姓名',
    gender        TINYINT      NOT NULL DEFAULT 0                 COMMENT '性别：0 未知 / 1 男 / 2 女',
    phone         VARCHAR(20)  DEFAULT NULL                       COMMENT '手机号',
    email         VARCHAR(100) DEFAULT NULL                       COMMENT '邮箱',
    role_id       BIGINT       NOT NULL                           COMMENT '所属角色，登录后据此鉴权',
    status        TINYINT      NOT NULL DEFAULT 1                 COMMENT '账号状态：1 启用 / 0 停用，停用后不能登录',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP       COMMENT '最后修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id)
        REFERENCES role(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================================
-- 3. meeting_room（会议室表）
-- 会议室"占用状态"不落库，由 booking 中状态为 0、1 的记录推导，
-- 避免与预约表不一致。status=0 停用后不可被新预约，已有预约不受影响。
-- =====================================================================
CREATE TABLE meeting_room (
    id          BIGINT       NOT NULL AUTO_INCREMENT            COMMENT '会议室主键',
    room_no     VARCHAR(20)  NOT NULL                           COMMENT '会议室编号，如 A101',
    name        VARCHAR(100) NOT NULL                           COMMENT '会议室名称',
    location    VARCHAR(200) NOT NULL                           COMMENT '位置，如 A 座 1 楼',
    capacity    INT          NOT NULL                           COMMENT '容纳人数',
    status      TINYINT      NOT NULL DEFAULT 1                 COMMENT '状态：1 启用 / 0 停用，停用后不可被新预约',
    description VARCHAR(300) DEFAULT NULL                       COMMENT '备注',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP       COMMENT '最后修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_room_no (room_no),
    CONSTRAINT chk_capacity CHECK (capacity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室表';

-- =====================================================================
-- 4. facility（设备表）
-- 独立维护的基础数据，供会议室多选挂载与查询筛选使用。
-- =====================================================================
CREATE TABLE facility (
    id         BIGINT       NOT NULL AUTO_INCREMENT            COMMENT '设备主键',
    name       VARCHAR(50)  NOT NULL                           COMMENT '设备名称，如投影仪、白板',
    remark     VARCHAR(200) DEFAULT NULL                       COMMENT '备注',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_facility_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- =====================================================================
-- 5. meeting_room_facility（会议室-设备关联表）
-- 纯关联表，两列均为组合主键，保证"同一会议室不会重复挂同一设备"。
-- 外键级联删除：删会议室或设备时自动清除关联记录。
-- =====================================================================
CREATE TABLE meeting_room_facility (
    room_id    BIGINT NOT NULL COMMENT '会议室',
    facility_id BIGINT NOT NULL COMMENT '设备',
    PRIMARY KEY (room_id, facility_id),
    CONSTRAINT fk_mrf_room FOREIGN KEY (room_id)
        REFERENCES meeting_room(id) ON DELETE CASCADE,
    CONSTRAINT fk_mrf_facility FOREIGN KEY (facility_id)
        REFERENCES facility(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室-设备关联表';

-- =====================================================================
-- 6. time_slot（预置时段表）
-- 管理员维护的固定时段模板，普通用户只能从中选择。
-- start_time / end_time 用 TIME（每日重复的时刻模板）。
-- =====================================================================
CREATE TABLE time_slot (
    id                BIGINT      NOT NULL AUTO_INCREMENT            COMMENT '时段主键',
    slot_name         VARCHAR(50) NOT NULL                           COMMENT '时段名称，如"上午第 1 时段"',
    start_time        TIME        NOT NULL                           COMMENT '时段开始时刻，如 08:00',
    end_time          TIME        NOT NULL                           COMMENT '时段结束时刻，如 09:00',
    duration_minutes  INT         NOT NULL                           COMMENT '时长（分钟）：30 / 45 / 60',
    status            TINYINT     NOT NULL DEFAULT 1                 COMMENT '状态：1 启用 / 0 停用',
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP       COMMENT '最后修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_slot_time (start_time, end_time),
    CONSTRAINT chk_slot_order CHECK (end_time > start_time),
    CONSTRAINT chk_duration   CHECK (duration_minutes > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预置时段表';

-- =====================================================================
-- 7. booking（预约表）—— 全库最核心
-- 按方案 B 固定时段方式下单：用户选预置时段 → 服务端换算成
-- booking_date + start_time + end_time 存绝对时刻快照。
-- 半开区间 [start, end) 冲突判定，首尾相接不算冲突。
--
-- status 取值：
--   0 待审核（占用时段）   1 已预约（占用时段）
--   2 已驳回（释放时段）   3 已取消（释放时段）   4 已结束（释放时段）
-- =====================================================================
CREATE TABLE booking (
    id                BIGINT       NOT NULL AUTO_INCREMENT            COMMENT '预约主键',
    booking_no        VARCHAR(32)  DEFAULT NULL                       COMMENT '预约编号，审核通过时生成；待审核阶段为空',
    user_id           BIGINT       NOT NULL                           COMMENT '预约人',
    room_id           BIGINT       NOT NULL                           COMMENT '会议室',
    slot_id           BIGINT       DEFAULT NULL                       COMMENT '下单时选择的预置时段，原时段被删除时为空',
    booking_date      DATE         NOT NULL                           COMMENT '预约日期，用于按天查询',
    start_time        DATETIME     NOT NULL                           COMMENT '开始时刻，提交时的快照',
    end_time          DATETIME     NOT NULL                           COMMENT '结束时刻，必须晚于 start_time',
    purpose           VARCHAR(200) NOT NULL                           COMMENT '用途（会议主题）',
    status            TINYINT      NOT NULL DEFAULT 0                 COMMENT '状态：0 待审核 / 1 已预约 / 2 已驳回 / 3 已取消 / 4 已结束',
    reject_reason     VARCHAR(200) DEFAULT NULL                       COMMENT '驳回原因，status=2 时有值',
    cancel_reason     VARCHAR(200) DEFAULT NULL                       COMMENT '取消原因，status=3 时有值',
    reschedule_from_id BIGINT      DEFAULT NULL                       COMMENT '改期来源：本条由哪条预约改期而来',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                ON UPDATE CURRENT_TIMESTAMP       COMMENT '最后修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_booking_no (booking_no),
    INDEX idx_booking_room_date (room_id, booking_date),
    CONSTRAINT fk_booking_user   FOREIGN KEY (user_id)
        REFERENCES user(id) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_room   FOREIGN KEY (room_id)
        REFERENCES meeting_room(id) ON DELETE RESTRICT,
    CONSTRAINT fk_booking_slot   FOREIGN KEY (slot_id)
        REFERENCES time_slot(id) ON DELETE SET NULL,
    CONSTRAINT fk_booking_resched FOREIGN KEY (reschedule_from_id)
        REFERENCES booking(id) ON DELETE RESTRICT,
    CONSTRAINT chk_booking_time CHECK (end_time > start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- =====================================================================
-- 8. booking_audit_log（审核日志表）
-- 只增不改不删，记录操作人、操作时间、操作类型与前后状态。
-- operator_id 允许为空：系统自动置为已结束时无操作人。
--
-- action 取值：1 提交 / 2 通过 / 3 驳回 / 4 取消 / 5 改期 / 6 结束
-- =====================================================================
CREATE TABLE booking_audit_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT            COMMENT '日志主键',
    booking_id  BIGINT       NOT NULL                           COMMENT '被操作的预约',
    operator_id BIGINT       DEFAULT NULL                       COMMENT '操作人；系统自动操作时为空',
    action      TINYINT      NOT NULL                           COMMENT '操作类型：1 提交 / 2 通过 / 3 驳回 / 4 取消 / 5 改期 / 6 结束',
    from_status TINYINT      DEFAULT NULL                       COMMENT '操作前状态；提交时为 NULL',
    to_status   TINYINT      NOT NULL                           COMMENT '操作后状态',
    reason      VARCHAR(200) DEFAULT NULL                       COMMENT '驳回原因或处理说明',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    CONSTRAINT fk_log_booking  FOREIGN KEY (booking_id)
        REFERENCES booking(id) ON DELETE RESTRICT,
    CONSTRAINT fk_log_operator FOREIGN KEY (operator_id)
        REFERENCES user(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核日志表';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 初始化数据
-- =====================================================================

-- 角色：USER（普通用户）、ADMIN（管理员）
INSERT INTO role (role_code, role_name, description) VALUES
    ('USER',  '普通用户', '可查看会议室、提交和管理本人预约'),
    ('ADMIN', '管理员',   '可管理用户、会议室、时段、审核全部预约');

-- 用户：1 个普通用户 + 1 个管理员
-- 密码统一为 "123456"，此处存 BCrypt 哈希值（版本 $2b$12$）。
-- 测试口令写在 README 里，脚本中不出现明文。
-- 哈希由 bcrypt-python 生成：bcrypt.hashpw(b'123456', bcrypt.gensalt())
INSERT INTO user (username, password_hash, real_name, gender, phone, email, role_id, status) VALUES
    ('admin',
     '$2b$12$n8QsckE0QDkTCzuI8mf6I.CCspn.4Kz/Dxpd1LkBHFnbfKiVvdASW',
     '系统管理员', 1, '13800000000', 'admin@meeting-room.local', 2, 1),
    ('user1',
     '$2b$12$vSXw1Pp3lO96rdO49GHHf.LA7TEv0UAU7xSxRQLJmxtTajHfXPMDO',
     '张三', 1, '13900000001', 'user1@meeting-room.local', 1, 1);

-- 设备：3 种基础设备
INSERT INTO facility (name, remark) VALUES
    ('投影仪',      '标准高清投影仪'),
    ('电子白板',    '触控电子白板'),
    ('视频会议终端', '支持 4K 视频会议');

-- 会议室：至少 3 间，room_no 唯一
INSERT INTO meeting_room (room_no, name, location, capacity, status, description) VALUES
    ('A101', 'A101 会议室', 'A 座 1 楼东', 10, 1, '小型讨论室'),
    ('B201', 'B201 会议室', 'B 座 2 楼中', 20, 1, '中型会议室'),
    ('C301', 'C301 会议室', 'C 座 3 楼西', 30, 1, '大型会议室，带视频会议');

-- 会议室 ↔ 设备 关联
INSERT INTO meeting_room_facility (room_id, facility_id) VALUES
    (1, 1),  -- A101 有投影仪
    (1, 2),  -- A101 有白板
    (2, 1),  -- B201 有投影仪
    (3, 1),  -- C301 有投影仪
    (3, 2),  -- C301 有白板
    (3, 3);  -- C301 有视频会议终端

-- 预置时段：每天固定的时段模板，程序按此换算成绝对时刻
INSERT INTO time_slot (slot_name, start_time, end_time, duration_minutes, status) VALUES
    ('上午第 1 时段', '08:00:00', '09:00:00', 60, 1),
    ('上午第 2 时段', '09:00:00', '10:00:00', 60, 1),
    ('上午第 3 时段', '10:00:00', '11:00:00', 60, 1),
    ('上午第 4 时段', '11:00:00', '12:00:00', 60, 1),
    ('下午第 1 时段', '14:00:00', '15:00:00', 60, 1),
    ('下午第 2 时段', '15:00:00', '16:00:00', 60, 1),
    ('下午第 3 时段', '16:00:00', '17:00:00', 60, 1);

-- ---------------------------------------------------------------------
-- 预约数据：1 组可复现的冲突预约 + 1 条正常预约
--
-- 冲突预约（用于课上演示半开区间判定）：
--   booking 1：A101，[09:00, 11:00)，已预约
--   booking 2：A101，[10:00, 12:00)，已预约
--   判定：start_new(10:00) < end_old(11:00) 且 end_new(12:00) > start_old(09:00) → 冲突 ✓
--
-- 非冲突预约（首尾相接）：
--   booking 3：A101，[11:00, 13:00)，已预约
--   与 booking 1 首尾相接：booking1.end = booking3.start → 不算冲突 ✓
-- ---------------------------------------------------------------------
INSERT INTO booking (
    user_id, room_id, slot_id,
    booking_date, start_time, end_time,
    purpose, status
) VALUES
    -- 冲突对第 1 条：09:00 - 11:00
    (2, 1, 4, '2026-11-02', '2026-11-02 09:00:00', '2026-11-02 11:00:00', '项目周会',     1),
    -- 冲突对第 2 条：10:00 - 12:00  ← 与上一条重叠 10:00-11:00
    (1, 1, 4, '2026-11-02', '2026-11-02 10:00:00', '2026-11-02 12:00:00', '产品评审会',   1),
    -- 首尾相接：11:00 - 13:00  ← 与第 1 条恰好衔接，不算冲突
    (2, 1, 4, '2026-11-02', '2026-11-02 11:00:00', '2026-11-02 13:00:00', '代码走查',     1),
    -- 不同会议室：B201 下午一条正常预约
    (1, 2, 6, '2026-11-02', '2026-11-02 14:00:00', '2026-11-02 16:00:00', '部门总结会',   1),
    -- 已驳回的预约（用于展示驳回状态）
    (2, 3, 4, '2026-11-03', '2026-11-03 14:00:00', '2026-11-03 15:00:00', '临时小组讨论', 2),
    -- 已取消的预约
    (1, 1, 4, '2026-11-04', '2026-11-04 09:00:00', '2026-11-04 10:00:00', '已取消的会议', 3);

-- 为已有预约生成审核日志
INSERT INTO booking_audit_log (booking_id, operator_id, action, from_status, to_status, reason) VALUES
    -- 冲突预约第 1 条：user1 提交 → admin 通过
    (1,  2, 1, NULL, 0, NULL),
    (1,  1, 2, 0,    1, NULL),
    -- 冲突预约第 2 条：admin 提交 → admin 通过
    (2,  2, 1, NULL, 0, NULL),
    (2,  1, 2, 0,    1, NULL),
    -- 首尾相接：user1 提交 → admin 通过
    (3,  2, 1, NULL, 0, NULL),
    (3,  1, 2, 0,    1, NULL),
    -- B201 正常预约：admin 提交 → admin 通过
    (4,  1, 1, NULL, 0, NULL),
    (4,  1, 2, 0,    1, NULL),
    -- 已驳回：user1 提交 → admin 驳回，原因"时段与已有预约冲突"
    (5,  2, 1, NULL, 0, NULL),
    (5,  1, 3, 0,    2, '该时段已有 C301 会议室预约，请改期'),
    -- 已取消：admin 提交 → admin 取消
    (6,  1, 1, NULL, 0, NULL),
    (6,  1, 2, 0,    1, NULL),
    (6,  1, 4, 1,    3, '会议取消，参与者时间冲突');

-- 验证（可忽略以下 SELECT，课上 source 后直接查询即可）
SELECT 'role'    AS tbl, COUNT(*) AS cnt FROM role
UNION ALL SELECT 'user',            COUNT(*) FROM user
UNION ALL SELECT 'meeting_room',    COUNT(*) FROM meeting_room
UNION ALL SELECT 'facility',        COUNT(*) FROM facility
UNION ALL SELECT 'meeting_room_facility', COUNT(*) FROM meeting_room_facility
UNION ALL SELECT 'time_slot',       COUNT(*) FROM time_slot
UNION ALL SELECT 'booking',         COUNT(*) FROM booking
UNION ALL SELECT 'booking_audit_log',COUNT(*) FROM booking_audit_log;
