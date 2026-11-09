# -*- coding: utf-8 -*-
"""第 11 周功能测试用例（接口部分，9 条）

适配 2 条用 Playwright 截图单独执行，部署 2 条按 README 走查单独执行；
本脚本覆盖：功能 3 条 + 异常 2 条 + 权限 3 条 + 版本核对 1 条。
每条真实发请求并核对库内数据，证据写入 functional_cases.json。
"""
import subprocess

from api_common import api, db_query, get_token, now_str, save_evidence

results = []


def case(no, category, steps, expect, passed, actual, extra=None):
    item = {"no": no, "category": category, "steps": steps, "expect": expect,
            "actual": actual, "conclusion": "通过" if passed else "未通过",
            "passed": bool(passed)}
    if extra:
        item["evidence"] = extra
    results.append(item)
    print(f"[{no}] {category} {'✓' if passed else '✗'} {expect[:40]}")


def main():
    print(f"=== 功能用例执行 {now_str()} ===")
    user_token = get_token("user1", "123456")
    admin_token = get_token("admin", "123456")

    # ---------- 功能类 ----------
    # TC-F1 登录
    s, b, _ = api("POST", "/api/login", body={"username": "user1", "password": "123456"})
    d = b.get("data") or {}
    case("TC-F1", "功能", "POST /api/login {user1/123456}",
         "返回 code=0、token、角色 USER 与菜单",
         s == 200 and b.get("code") == 0 and d.get("token") and d.get("role"),
         f"http={s} role={d.get('role')} realName={d.get('realName')} 菜单数={len(d.get('menus') or [])}",
         extra={"response": b})

    # TC-F2 提交预约成功闭环（会议室3 / 2027-02-04 / 时段5）
    s, b, _ = api("POST", "/api/bookings", token=user_token,
                  body={"roomId": 3, "date": "2027-02-04", "timeSlotId": 5,
                        "purpose": "第11周功能测试-需求评审会"})
    bid = (b.get("data") or {}).get("bookingId")
    row = db_query("SELECT id, status, purpose FROM booking WHERE id=%s", (bid,))
    log = db_query("SELECT action, to_status FROM booking_audit_log WHERE booking_id=%s", (bid,))
    s2, b2, _ = api("GET", "/api/bookings/mine", token=user_token)
    in_mine = any(x.get("id") == bid for x in (b2.get("data") or []))
    case("TC-F2", "功能",
         "POST /api/bookings 提交 C301 2027-02-04 14:00-15:00，再查 /api/bookings/mine",
         "返回 code=0 与预约号；库内 status=1；日志 action=1；我的预约列表含该条",
         (s == 200 and b.get("code") == 0 and row and row[0]["status"] == 1
          and any(l["action"] == 1 for l in log) and in_mine),
         f"http={s} bookingId={bid} 库内status={row[0]['status'] if row else None} 我的列表含该条={in_mine}",
         extra={"response": b, "db_row": row, "audit_log": log})

    # TC-F3 修改用途 + 取消本人预约
    s, b, _ = api("PUT", f"/api/bookings/{bid}", token=user_token,
                  body={"purpose": "第11周功能测试-需求评审会(改)"})
    row = db_query("SELECT purpose FROM booking WHERE id=%s", (bid,))
    s2, b2, _ = api("DELETE", f"/api/bookings/{bid}", token=user_token,
                    body={"reason": "议程取消"})
    row2 = db_query("SELECT status, cancel_reason FROM booking WHERE id=%s", (bid,))
    case("TC-F3", "功能",
         f"PUT /api/bookings/{bid} 改用途；DELETE /api/bookings/{bid} 取消",
         "修改后库内用途更新；取消后 status=3 且记录取消原因",
         (s == 200 and row and "改" in row[0]["purpose"]
          and s2 == 200 and row2 and row2[0]["status"] == 3),
         f"修改http={s} 取消http={s2} 库内status={row2[0]['status'] if row2 else None} "
         f"原因={row2[0]['cancel_reason'] if row2 else None}",
         extra={"db_after_cancel": row2})

    # ---------- 异常类 ----------
    # TC-E1 错误密码登录
    s, b, _ = api("POST", "/api/login", body={"username": "user1", "password": "wrong999"})
    case("TC-E1", "异常", "POST /api/login {user1/wrong999}",
         "返回 401，提示账号或密码错误，不发 token",
         s == 401 and not (b.get("data") or {}).get("token"),
         f"http={s} msg={b.get('msg')}", extra={"response": b})

    # TC-E2 冲突时段 + 非法时段
    api("POST", "/api/bookings", token=user_token,
        body={"roomId": 2, "date": "2027-02-05", "timeSlotId": 3, "purpose": "E2基准预约"})
    s, b, _ = api("POST", "/api/bookings", token=user_token,
                  body={"roomId": 2, "date": "2027-02-05", "timeSlotId": 3,
                        "purpose": "E2冲突预约"})
    s2, b2, _ = api("POST", "/api/bookings", token=user_token,
                    body={"roomId": 2, "date": "2027-02-05", "timeSlotId": 999,
                          "purpose": "E2非法时段"})
    case("TC-E2", "异常",
         "同时段重复提交；timeSlotId=999 提交",
         "重复提交 409 时段冲突；非法时段 400 参数不合法",
         s == 409 and s2 == 400,
         f"冲突http={s}({b.get('msg')}) 非法时段http={s2}({b2.get('msg')})",
         extra={"conflict_response": b, "invalid_slot_response": b2})

    # ---------- 权限类 ----------
    # TC-P1 未登录访问
    s, b, _ = api("GET", "/api/rooms")
    case("TC-P1", "权限", "不带 token 访问 GET /api/rooms",
         "返回 401 未登录",
         s == 401, f"http={s} msg={b.get('msg') if isinstance(b, dict) else b}",
         extra={"response": b})

    # TC-P2 普通用户访问管理员接口
    s, b, _ = api("GET", "/api/admin/bookings", token=user_token)
    case("TC-P2", "权限", "user1 token 访问 GET /api/admin/bookings",
         "返回 403 无权限",
         s == 403, f"http={s} msg={b.get('msg') if isinstance(b, dict) else b}",
         extra={"response": b})

    # TC-P3 管理员取消：不填原因 400，填原因 200
    s0, b0, _ = api("POST", "/api/bookings", token=user_token,
                    body={"roomId": 3, "date": "2027-02-06", "timeSlotId": 6,
                          "purpose": "P3 待管理员处理的异常预约"})
    pid = (b0.get("data") or {}).get("bookingId")
    s, b, _ = api("PUT", f"/api/admin/bookings/{pid}/cancel", token=admin_token, body={})
    s2, b2, _ = api("PUT", f"/api/admin/bookings/{pid}/cancel", token=admin_token,
                    body={"reason": "重复预约，管理员清理"})
    row = db_query("SELECT status, cancel_reason FROM booking WHERE id=%s", (pid,))
    case("TC-P3", "权限",
         f"admin 取消预约 {pid}：先不填原因，再填原因",
         "不填原因 400；填原因 200，库内 status=3 且记录原因",
         (s == 400 and s2 == 200 and row and row[0]["status"] == 3
          and row[0]["cancel_reason"] == "重复预约，管理员清理"),
         f"无原因http={s} 有原因http={s2} 库内status={row[0]['status'] if row else None}",
         extra={"db_row": row})

    # ---------- 部署类（版本核对，走查见报告 TC-D1） ----------
    def ver(cmd):
        try:
            out = subprocess.run(cmd, capture_output=True, text=True, timeout=30,
                                 shell=True)
            return (out.stdout + out.stderr).strip().splitlines()[0]
        except Exception as e:  # noqa: BLE001
            return str(e)

    versions = {"java": ver("java -version"), "maven": ver("mvn -version"),
                "node": ver("node -v"), "mysql": ver("mysql --version")}
    ok = ("17" in versions["java"] and "3.9" in versions["maven"]
          and versions["node"].startswith("v") and "8." in versions["mysql"])
    case("TC-D2", "部署", "核对 java/maven/node/mysql 版本输出",
         "JDK 17、Maven 3.9+、Node LTS、MySQL 8.x，与 README 版本要求一致",
         ok, str(versions), extra=versions)

    save_evidence("functional_cases.json", {
        "executed_at": now_str(),
        "cases": results,
        "passed": sum(1 for r in results if r["passed"]),
        "total": len(results),
    })
    print(f"=== 功能用例完成：{sum(1 for r in results if r['passed'])}/{len(results)} 通过 ===")


if __name__ == "__main__":
    main()
