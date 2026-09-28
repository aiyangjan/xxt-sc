package com.xxt.sc.common.result;

/**
 * 业务错误码。
 *
 * <p>PRD 7.3 要求：错误响应必须包含业务错误码、可读消息、追踪标识和是否可重试。
 * retryable=true 的异常才允许上游/任务队列自动重试，避免对不可重试错误造成重复扣款或重复入账。
 */
public enum ErrorCode {

    OK("0", "成功", false),

    PARAM_ERROR("40000", "参数错误", false),
    UNAUTHORIZED("40100", "未登录或会话已失效", false),
    FORBIDDEN("40300", "无权限执行该操作", false),
    FIELD_FORBIDDEN("40301", "无权限查看该字段", false),
    SUPPLIER_SCOPE_DENIED("40302", "超出供应商数据范围", false),

    SKU_OFF_SHELF("40901", "SKU 已下架，不可下单", false),
    INSUFFICIENT_STOCK("40902", "库存不足，不可创建可支付订单", false),
    PRICE_EXPIRED("40903", "价格已过期，请重新下单", false),
    ORDER_NOT_PAYABLE("40904", "订单当前状态不可支付", false),
    ORDER_NOT_FOUND("40401", "订单不存在或无权查看", false),
    ORDER_NOT_CANCELABLE("40908", "订单当前状态不可取消", false),
    REFUND_EXCEED_LIMIT("40905", "退款数量或金额超出可退上限", false),
    REFUND_DUPLICATE("40906", "同一订单行存在处理中的重复退款", false),
    IDEMPOTENT_CONFLICT("40907", "幂等键冲突，请求正在处理或已处理", false),

    THIRD_PARTY_TIMEOUT("50400", "第三方调用超时", true),
    THIRD_PARTY_UNAVAILABLE("50401", "第三方暂不可用", true),
    ERP_SYNC_PENDING("50402", "ERP 同步未确认，进入重试队列", true),

    INTERNAL_ERROR("50000", "系统内部错误", true);

    private final String code;
    private final String message;
    private final boolean retryable;

    ErrorCode(String code, String message, boolean retryable) {
        this.code = code;
        this.message = message;
        this.retryable = retryable;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
