package com.meetingroom.common;

import lombok.Data;

/**
 * 统一响应结果
 * 前端约定格式：{ code: 0/1, msg: "...", data: {...} }
 * code=0 成功，code=1 失败
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

    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.setCode(1);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }
}
