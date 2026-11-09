# -*- coding: utf-8 -*-
"""第 11 周破坏性测试：四类攻击，每类留 请求 / 响应 / 库内数据 三样证据

A1 越权取消：普通用户取消他人预约、普通用户调用管理员取消接口
A2 越权查看：普通用户访问管理员预约列表；我的预约仅含本人数据
A3 时间边界：首尾相接必须放行、完全重复必须 409、改期撞单必须 409
A4 SQL 注入：登录注入、筛选注入、用途字段注入、路径参数注入
"""
from api_common import (api, db_query, force_delete_bookings, get_token,
                        now_str, save_evidence)

evidence = {"test": "四类破坏性攻击测试", "executed_at": now_str(), "attacks": []}


def record(name, req, resp_status, resp_body, db_check):
    evidence["attacks"].append({
        "attack": name,
        "request": req,
        "response": {"http_status": resp_status, "body": resp_body},
        "db_check": db_check,
    })
    print(f"[{name}] HTTP {resp_status} -> {resp_body.get('msg') if isinstance(resp_body, dict) else resp_body}")


def main():
    user_token = get_token("user1", "123456")
    admin_token = get_token("admin", "123456")

    # ---------------- A1 越权取消 ----------------
    # 找一条 admin(user_id=1) 的预约作为受害者
    target = db_query("SELECT id, status FROM booking WHERE user_id=1 LIMIT 1")[0]
    before = db_query("SELECT id, status, cancel_reason FROM booking WHERE id=%s", (target["id"],))
    s, b, _ = api("DELETE", f"/api/bookings/{target['id']}", token=user_token,
                  body={"reason": "越权尝试取消"})
    after = db_query("SELECT id, status, cancel_reason FROM booking WHERE id=%s", (target["id"],))
    record("A1-1 普通用户取消他人预约",
           {"method": "DELETE", "path": f"/api/bookings/{target['id']}",
            "auth": "user1（预约属 admin）", "body": {"reason": "越权尝试取消"}},
           s, b, {"before": before, "after": after,
                  "unchanged": before == after, "expect": "403 且库内状态不变",
                  "passed": s == 403 and before == after})

    s, b, _ = api("PUT", f"/api/admin/bookings/{target['id']}/cancel", token=user_token,
                  body={"reason": "越权调用管理员接口"})
    after2 = db_query("SELECT id, status FROM booking WHERE id=%s", (target["id"],))
    record("A1-2 普通用户调用管理员取消接口",
           {"method": "PUT", "path": f"/api/admin/bookings/{target['id']}/cancel",
            "auth": "user1", "body": {"reason": "越权调用管理员接口"}},
           s, b, {"after": after2, "unchanged": after2[0]["status"] == target["status"],
                  "expect": "403 且库内状态不变",
                  "passed": s == 403 and after2[0]["status"] == target["status"]})

    # ---------------- A2 越权查看 ----------------
    s, b, _ = api("GET", "/api/admin/bookings", token=user_token)
    record("A2-1 普通用户访问管理员预约列表",
           {"method": "GET", "path": "/api/admin/bookings", "auth": "user1"},
           s, b, {"expect": "403 且不返回任何预约数据",
                  "leaked": bool(isinstance(b, dict) and b.get("data")),
                  "passed": s == 403})

    s, b, _ = api("GET", "/api/bookings/mine", token=user_token)
    mine = b.get("data") or []
    foreign = [x for x in mine if x.get("userId") != 2]
    record("A2-2 我的预约仅含本人数据",
           {"method": "GET", "path": "/api/bookings/mine", "auth": "user1"},
           s, {"code": b.get("code"), "total": len(mine)},
           {"returned": len(mine), "foreign_rows": len(foreign),
            "expect": "200 且全部 user_id=2", "passed": s == 200 and not foreign})

    # ---------------- A3 时间边界 ----------------
    force_delete_bookings("room_id=1 AND booking_date='2027-02-02'")
    # 先占 09:00-10:00（时段2）
    s1, b1, _ = api("POST", "/api/bookings", token=user_token,
                    body={"roomId": 1, "date": "2027-02-02", "timeSlotId": 2,
                          "purpose": "边界测试基准 09:00-10:00"})
    bid = (b1.get("data") or {}).get("bookingId")
    # 首尾相接：08:00-09:00（时段1）必须放行
    s2, b2, _ = api("POST", "/api/bookings", token=user_token,
                    body={"roomId": 1, "date": "2027-02-02", "timeSlotId": 1,
                          "purpose": "边界测试 首尾相接 08:00-09:00"})
    # 完全重复：再订 09:00-10:00 必须 409
    s3, b3, _ = api("POST", "/api/bookings", token=user_token,
                    body={"roomId": 1, "date": "2027-02-02", "timeSlotId": 2,
                          "purpose": "边界测试 完全重复"})
    # 改期撞单：把已订的 08:00-09:00 改成 09:00-10:00 必须 409
    bid2 = (b2.get("data") or {}).get("bookingId")
    s4, b4, _ = api("PUT", f"/api/bookings/{bid2}", token=user_token,
                    body={"timeSlotId": 2})
    rows = db_query(
        "SELECT id, start_time, end_time, status FROM booking "
        "WHERE room_id=1 AND booking_date='2027-02-02' AND status IN (0,1) "
        "ORDER BY start_time")
    ok = (s1 == 200 and s2 == 200 and s3 == 409 and s4 == 409 and len(rows) == 2)
    record("A3 时间边界（首尾相接放行 / 重复与改期撞单 409）",
           {"method": "POST/PUT", "path": "/api/bookings",
            "body": "基准09:00-10:00；相接08:00-09:00；重复09:00-10:00；改期->09:00-10:00"},
           f"基准{s1} 相接{s2} 重复{s3} 改期{s4}",
           {"基准": b1, "相接": b2, "重复": b3, "改期": b4},
           {"occupied_rows": rows, "expect": "库内恰好 [08:00-09:00, 09:00-10:00] 两条且不重叠",
            "passed": ok})

    # ---------------- A4 SQL 注入 ----------------
    s, b, _ = api("POST", "/api/login",
                  body={"username": "admin' OR '1'='1", "password": "whatever"})
    record("A4-1 登录注入 admin' OR '1'='1",
           {"method": "POST", "path": "/api/login",
            "body": {"username": "admin' OR '1'='1", "password": "whatever"}},
           s, b, {"expect": "401 登录失败，不能绕过认证", "passed": s == 401})

    s, b, _ = api("GET", "/api/rooms", token=user_token,
                  params={"name": "' OR '1'='1"})
    rooms = b.get("data") or []
    record("A4-2 会议室筛选注入 name=' OR '1'='1",
           {"method": "GET", "path": "/api/rooms?name=' OR '1'='1", "auth": "user1"},
           s, {"code": b.get("code"), "returned": len(rooms)},
           {"expect": "按字面 LIKE 匹配，返回 0 条，不能拖出全部数据",
            "passed": s == 200 and len(rooms) == 0})

    cnt_before = db_query("SELECT COUNT(*) AS c FROM booking")[0]["c"]
    s, b, _ = api("POST", "/api/bookings", token=user_token,
                  body={"roomId": 2, "date": "2027-02-03", "timeSlotId": 5,
                        "purpose": "x'); DROP TABLE booking;--"})
    inj_id = (b.get("data") or {}).get("bookingId")
    table_alive = db_query("SHOW TABLES LIKE 'booking'")
    cnt_after = db_query("SELECT COUNT(*) AS c FROM booking")[0]["c"]
    record("A4-3 用途字段注入 x'); DROP TABLE booking;--",
           {"method": "POST", "path": "/api/bookings", "auth": "user1",
            "body": {"roomId": 2, "date": "2027-02-03", "timeSlotId": 5,
                     "purpose": "x'); DROP TABLE booking;--"}},
           s, b,
           {"table_booking_exists": bool(table_alive),
            "count_before": cnt_before, "count_after": cnt_after,
            "expect": "booking 表完好；注入串若落库仅为普通字符串",
            "passed": bool(table_alive)})

    s, b, _ = api("DELETE", "/api/bookings/1%20OR%201%3D1", token=user_token)
    record("A4-4 路径参数注入 /api/bookings/1 OR 1=1",
           {"method": "DELETE", "path": "/api/bookings/1 OR 1=1", "auth": "user1"},
           s, b, {"expect": "id 类型校验拦截（期望 400），不能按条件批量删除",
                  "note": "若返回 500 说明类型异常被全局兜底，记为缺陷",
                  "passed": s in (400, 404)})

    # 清理本次攻击造的数据并核对
    cleaned = 0
    if inj_id:
        api("DELETE", f"/api/bookings/{inj_id}", token=user_token, body={"reason": "攻击测试清理"})
        cleaned += 1
    force_delete_bookings("room_id=1 AND booking_date='2027-02-02'")
    final_cnt = db_query("SELECT COUNT(*) AS c FROM booking")[0]["c"]
    evidence["cleanup"] = {
        "note": "A3 边界数据按 room_id+date 删除；A4-3 注入预约通过正常取消接口删除",
        "booking_count_final": final_cnt,
    }
    evidence["summary"] = {
        "total": len(evidence["attacks"]),
        "passed": sum(1 for a in evidence["attacks"] if a["db_check"].get("passed")),
    }
    save_evidence("attacks.json", evidence)
    print(f"=== 攻击测试完成，共 {len(evidence['attacks'])} 项，证据见 attacks.json ===")


if __name__ == "__main__":
    main()
