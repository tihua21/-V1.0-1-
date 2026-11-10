# -*- coding: utf-8 -*-
"""第 11 周非功能测试 ①：50 并发同一秒抢订同一会议室同一时段

要求：50 个请求必须同一秒同时发起（不是 5 个、不是顺序发），
预期：恰好 1 个成功（code=0），49 个明确失败（409 冲突），
库内该会议室该日期该时段占用中的预约必须恰好 1 条（无超卖）。
"""
import threading
import time

from api_common import api, db_query, force_delete_bookings, get_token, now_str, save_evidence

ROOM_ID = 1            # A101
DATE = "2027-02-01"    # 空闲未来日期
TIME_SLOT_ID = 3       # 10:00-11:00
THREADS = 50


def main():
    print(f"=== 50 并发抢订测试 {now_str()} ===")
    token = get_token("user1", "123456")

    # 清场：删除目标会议室/日期的历史数据，保证测试可重复
    cleaned = force_delete_bookings("room_id=%s AND booking_date=%s", (ROOM_ID, DATE))
    print(f"清场：删除 room_id={ROOM_ID} date={DATE} 的旧预约 {cleaned} 条")

    payload = {
        "roomId": ROOM_ID,
        "date": DATE,
        "timeSlotId": TIME_SLOT_ID,
        "purpose": "第11周50并发抢订测试",
    }
    barrier = threading.Barrier(THREADS)
    results = [None] * THREADS

    def worker(idx):
        barrier.wait(timeout=30)  # 50 个线程在此对齐，同一秒放行
        send_ts = time.time()
        status, body, ms = api("POST", "/api/bookings", token=token, body=payload)
        results[idx] = {
            "thread": idx,
            "send_epoch": round(send_ts, 3),
            "http_status": status,
            "code": body.get("code") if body else None,
            "msg": body.get("msg") if body else None,
            "bookingNo": (body.get("data") or {}).get("bookingNo") if body else None,
            "elapsed_ms": round(ms, 1),
        }

    threads = [threading.Thread(target=worker, args=(i,)) for i in range(THREADS)]
    t0 = time.time()
    for t in threads:
        t.start()
    for t in threads:
        t.join(timeout=60)
    wall_ms = (time.time() - t0) * 1000

    send_times = [r["send_epoch"] for r in results]
    spread_ms = (max(send_times) - min(send_times)) * 1000
    success = [r for r in results if r["code"] == 0]
    conflict = [r for r in results if r["http_status"] == 409]
    other = [r for r in results if r["code"] != 0 and r["http_status"] != 409]

    # 库内核对：占用中（status 0/1）的预约必须恰好 1 条
    db_rows = db_query(
        "SELECT id, booking_no, user_id, room_id, booking_date, start_time, end_time, status "
        "FROM booking WHERE room_id=%s AND booking_date=%s "
        "AND start_time='2027-02-01 10:00:00' AND end_time='2027-02-01 11:00:00' "
        "AND status IN (0,1)",
        (ROOM_ID, DATE),
    )

    evidence = {
        "test": "50 并发同一秒抢订 A101 2027-02-01 10:00-11:00",
        "executed_at": now_str(),
        "request": {"method": "POST", "path": "/api/bookings", "body": payload,
                    "auth": "user1 的 token（50 线程共用）"},
        "thread_count": THREADS,
        "send_time_spread_ms": round(spread_ms, 1),
        "wall_time_ms": round(wall_ms, 1),
        "success_count": len(success),
        "conflict_409_count": len(conflict),
        "other_count": len(other),
        "db_occupied_rows": db_rows,
        "db_occupied_count": len(db_rows),
        "all_responses": results,
        "passed": len(success) == 1 and len(conflict) == THREADS - 1
                  and len(other) == 0 and len(db_rows) == 1,
    }
    save_evidence("concurrency_50.json", evidence)

    print(f"发起时间差 spread={spread_ms:.1f}ms（同一秒内），总耗时 {wall_ms:.0f}ms")
    print(f"成功 {len(success)} / 409 冲突 {len(conflict)} / 其他 {len(other)}")
    print(f"库内占用记录 {len(db_rows)} 条")
    print("结论：" + ("通过 ✓（1 成功 49 失败，库内恰好 1 条，无超卖）"
                    if evidence["passed"] else "未通过 ✗"))


if __name__ == "__main__":
    main()
