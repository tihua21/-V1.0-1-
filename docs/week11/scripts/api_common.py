# -*- coding: utf-8 -*-
"""第 11 周测试脚本公共模块：HTTP 请求、登录、证据保存、数据库访问"""
import json
import os
import time

import pymysql
import requests

BASE_URL = "http://localhost:8080"

DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "123456",
    "database": "meeting_room",
    "charset": "utf8mb4",
}

EVIDENCE_DIR = os.path.normpath(
    os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "evidence")
)
os.makedirs(EVIDENCE_DIR, exist_ok=True)


def login(username, password):
    """登录，返回 (http_status, 整个响应json)。成功时 json["data"]["token"] 可用。"""
    resp = requests.post(
        BASE_URL + "/api/login",
        json={"username": username, "password": password},
        timeout=10,
    )
    return resp.status_code, resp.json()


def get_token(username, password):
    status, body = login(username, password)
    assert status == 200 and body.get("code") == 0, f"登录失败: {status} {body}"
    return body["data"]["token"]


def api(method, path, token=None, body=None, params=None, timeout=15):
    """统一请求入口，返回 (http_status, 响应json或None, 耗时ms)"""
    headers = {}
    if token:
        headers["Authorization"] = token
    start = time.perf_counter()
    resp = requests.request(
        method,
        BASE_URL + path,
        json=body,
        params=params,
        headers=headers,
        timeout=timeout,
    )
    elapsed_ms = (time.perf_counter() - start) * 1000
    try:
        payload = resp.json()
    except ValueError:
        payload = None
    return resp.status_code, payload, elapsed_ms


def db_query(sql, args=None):
    """执行查询，返回 list[dict]"""
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor(pymysql.cursors.DictCursor) as cur:
            cur.execute(sql, args)
            return cur.fetchall()
    finally:
        conn.close()


def db_execute(sql, args=None):
    """执行写操作，返回受影响行数"""
    conn = pymysql.connect(**DB_CONFIG)
    try:
        with conn.cursor() as cur:
            affected = cur.execute(sql, args)
        conn.commit()
        return affected
    finally:
        conn.close()


def force_delete_bookings(where_sql, args=None):
    """删除预约（先删关联审计日志，绕过 fk_log_booking 的 RESTRICT），返回删除的预约数"""
    db_execute(
        "DELETE l FROM booking_audit_log l JOIN booking b ON l.booking_id = b.id WHERE "
        + where_sql, args)
    return db_execute("DELETE FROM booking WHERE " + where_sql, args)


def save_evidence(filename, data):
    """把证据对象写入 docs/week11/evidence/<filename>"""
    path = os.path.join(EVIDENCE_DIR, filename)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2, default=str)
    print(f"[证据] 已保存 {path}")
    return path


def now_str():
    return time.strftime("%Y-%m-%d %H:%M:%S")
