package com.meetingroom.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理：未捕获异常统一返回 500，避免把堆栈直接暴露给前端
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 第11周 Bug 修复（BUG-W11-02）：路径/查询参数类型不匹配（如把字符串传给 Long 型的 {id}）
     * 属于客户端请求格式错误，返回 400 而不是落入 500 兜底
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(400)
                .body(Result.error(400, "请求参数格式不正确"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handle(Exception e) {
        return ResponseEntity.status(500)
                .body(Result.error(500, "服务器开小差了，请稍后重试"));
    }
}
