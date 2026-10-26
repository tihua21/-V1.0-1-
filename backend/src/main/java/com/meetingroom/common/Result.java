package com.meetingroom.common;

import lombok.Data;

/**
 * 统一响应结果
 * 格式：{ code: 0 或 HTTP 状态码, msg: "...", data: {...} }
 * code=0 成功；失败时 code 与 HTTP 状态码一致（400/401/403/409/500），
 * 错误提示文案严格按第 8 周定稿的接口清单。
 */
@Data
public class Result<T> {

    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(0);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> error(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }
}
