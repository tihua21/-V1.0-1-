# -*- coding: utf-8 -*-
"""第 11 周非功能测试 ②：性能测试

步骤：
1. 直接 SQL 灌入 1000 条预约（user1 名下，2027 年散布，status=1）
2. GET /api/rooms 与 GET /api/bookings/mine 各测 20 次，取 P95
3. 对两条核心 SQL 执行 EXPLAIN 并保存执行计划
4. 删除灌入的 1000 条数据，恢复现场，记录影响范围

达标线：两接口 P95 < 500ms；/api/bookings/mine 全量返回（不许只回前 10 条）
"""
import time

from api_common import (api, db_execute, db_query, get_token, now_str,
                        save_evidence, DB_CONFIG)
import pymysql

PERF_MARK = "PERF11-"   # 灌入数据标识：booking_no 前缀 / purpose 前缀
LOAD_COUNT = 1000
RUNS = 20
P95_LIMIT_MS = 500


def load_data():
    """灌入 1000 条预约，返回实际插入行数"""
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            rows = []
            for i in range(LOAD_COUNT):
                room_id = i % 3 + 1                      # 1/2/3 轮转
                slot_id = i % 6 + 1                      # 时段 1..6 轮转
                day = i % 28 + 1
                month = i % 10 + 1                       # 2027-01..10 散布
                date = f"2027-{month:02d}-{day:02d}"
                start_hour = 8 + (slot_id - 1) % 4 if slot_id <= 4 else 13 + slot_id - 4
                start = f"{date} {start_hour:02d}:00:00"
                end = f"{date} {start_hour + 1:02d}:00:00"
                rows.append((
                    f"{PERF_MARK}{i:04d}", 2, room_id, slot_id,
                    date, start, end, f"{PERF_MARK}性能灌数据{i:04d}", 1,
                ))
            sql = ("INSERT INTO booking (booking_no, user_id, room_id, slot_id, "
                   "booking_date, start_time, end_time, purpose, status) "
                   "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)")
            affected = cur.executemany(sql, rows)
        conn.commit()
        return affected
    finally:
        conn.close()


def cleanup_data():
    return db_execute("DELETE FROM booking WHERE booking_no LIKE %s", (f"{PERF_MARK}%",))


def measure(name, path, token):
    """同一接口连续测 RUNS 次，返回耗时列表(ms)与最后一次响应条数"""
    times, item_count = [], None
    for i in range(RUNS):
        status, body, ms = api("GET", path, token=token)
        assert status == 200 and body.get("code") == 0, f"{name} 第{i+1}次失败: {status} {body}"
        times.append(ms)
        item_count = len(body.get("data") or [])
    times_sorted = sorted(times)
    p95 = times_sorted[int(len(times_sorted) * 0.95) - 1]
    return {
        "endpoint": path,
        "runs": RUNS,
        "times_ms": [round(t, 1) for t in times],
        "avg_ms": round(sum(times) / len(times), 1),
        "max_ms": round(max(times), 1),
        "p95_ms": round(p95, 1),
        "returned_items": item_count,
        "p95_under_500ms": p95 < P95_LIMIT_MS,
    }


def main():
    print(f"=== 性能测试 {now_str()} ===")
    before = db_query("SELECT COUNT(*) AS c FROM booking")[0]["c"]
    inserted = load_data()
    after = db_query("SELECT COUNT(*) AS c FROM booking")[0]["c"]
    print(f"灌数据：{before} -> {after}（新增 {inserted} 条）")

    token = get_token("user1", "123456")
    # 预热 3 次，排除首次 JIT / 连接建立影响
    for _ in range(3):
        api("GET", "/api/rooms", token=token)
        api("GET", "/api/bookings/mine", token=token)

    rooms = measure("会议室列表", "/api/rooms", token)
    mine = measure("我的预约", "/api/bookings/mine", token)

    explain_rooms = db_query("EXPLAIN SELECT * FROM meeting_room ORDER BY id ASC")
    explain_mine = db_query(
        "EXPLAIN SELECT * FROM booking WHERE user_id=2 "
        "ORDER BY booking_date DESC, start_time DESC")

    deleted = cleanup_data()
    final = db_query("SELECT COUNT(*) AS c FROM booking")[0]["c"]
    print(f"恢复现场：删除 {deleted} 条灌入数据，booking 表回落到 {final} 条")

    evidence = {
        "test": "性能测试：1000 条预约下 /api/rooms 与 /api/bookings/mine 各 20 次取 P95",
        "executed_at": now_str(),
        "load": {"inserted": inserted, "count_before": before, "count_after": after},
        "results": [rooms, mine],
        "explain": {
            "rooms_sql": "EXPLAIN SELECT * FROM meeting_room ORDER BY id ASC",
            "rooms_plan": explain_rooms,
            "mine_sql": "EXPLAIN SELECT * FROM booking WHERE user_id=2 "
                        "ORDER BY booking_date DESC, start_time DESC",
            "mine_plan": explain_mine,
        },
        "cleanup": {"deleted": deleted, "count_final": final,
                    "impact": "灌入数据均以 PERF11- 前缀标识，测试后按前缀全部删除，"
                              "不影响既有种子数据与第 10 周冲突测试数据"},
        "passed": rooms["p95_under_500ms"] and mine["p95_under_500ms"]
                  and mine["returned_items"] >= 1000,
    }
    save_evidence("perf_p95.json", evidence)

    for r in (rooms, mine):
        print(f"{r['endpoint']}: avg={r['avg_ms']}ms p95={r['p95_ms']}ms "
              f"max={r['max_ms']}ms 返回{r['returned_items']}条 "
              f"{'✓' if r['p95_under_500ms'] else '✗ 超 500ms'}")
    print("结论：" + ("通过 ✓" if evidence["passed"] else "未通过 ✗"))


if __name__ == "__main__":
    main()
