package com.meetingroom.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：未捕获异常统一返回 500，避免把堆栈直接暴露给前端
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handle(Exception e) {
        return ResponseEntity.status(500)
                .body(Result.error(500, "服务器开小差了，请稍后重试"));
    }
}
