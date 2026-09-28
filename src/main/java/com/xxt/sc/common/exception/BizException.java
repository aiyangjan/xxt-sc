package com.xxt.sc.common.exception;

import com.xxt.sc.common.result.ErrorCode;

/**
 * 业务异常：仅用于可预期的业务拒绝，禁止用来控制正常流程。
 *
 * <p>继承 RuntimeException，Spring 声明式事务默认会对其回滚。
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
