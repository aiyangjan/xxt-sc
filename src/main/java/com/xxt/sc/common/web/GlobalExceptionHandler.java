package com.xxt.sc.common.web;

import com.xxt.sc.common.exception.BizException;
import com.xxt.sc.common.result.ApiResponse;
import com.xxt.sc.common.result.ErrorCode;
import com.xxt.sc.common.trace.TraceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把异常统一翻译成 ApiResponse，避免前端收到裸 500。
 *
 * <p>注意：业务异常按 200 返回并携带业务错误码（便于前端统一解析），
 * 只有真正未预期的系统错误才使用 5xx。第三方超时/不可用标记为 retryable，
 * 供上游任务队列判断是否重试。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException e) {
        String traceId = TraceId.current();
        log.warn("[{}] 业务拒绝 code={} msg={}", traceId,
                e.getErrorCode().getCode(), e.getMessage());
        return ApiResponse.fail(e.getErrorCode(), e.getMessage(), traceId);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValid(Exception e) {
        String traceId = TraceId.current();
        log.warn("[{}] 参数校验失败: {}", traceId, e.getMessage());
        return ApiResponse.fail(ErrorCode.PARAM_ERROR, "参数校验失败", traceId);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnexpected(Exception e) {
        String traceId = TraceId.current();
        log.error("[{}] 未预期异常", traceId, e);
        // 不把内部堆栈细节暴露给前端
        return ApiResponse.fail(ErrorCode.INTERNAL_ERROR, "服务暂时不可用，请稍后重试", traceId);
    }
}
