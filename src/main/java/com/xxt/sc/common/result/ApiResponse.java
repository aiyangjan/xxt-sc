package com.xxt.sc.common.result;

import java.time.Instant;

/**
 * 统一响应结构（PRD 7.3）。
 *
 * @param <T> 业务数据
 */
public class ApiResponse<T> {

    private String code;
    private String message;
    private String traceId;
    private boolean retryable;
    private Long timestamp;
    private T data;

    public ApiResponse() {
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = ErrorCode.OK.getCode();
        r.message = ErrorCode.OK.getMessage();
        r.retryable = false;
        r.timestamp = Instant.now().toEpochMilli();
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String traceId) {
        return fail(errorCode, errorCode.getMessage(), traceId);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message, String traceId) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = errorCode.getCode();
        r.message = message;
        r.retryable = errorCode.isRetryable();
        r.traceId = traceId;
        r.timestamp = Instant.now().toEpochMilli();
        return r;
    }

    public ApiResponse<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getTraceId() {
        return traceId;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public T getData() {
        return data;
    }
}
