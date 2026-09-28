package com.xxt.sc.common.trace;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * 追踪标识。PRD 7.3 要求错误响应必须携带 traceId，便于日志串联和客服定位。
 *
 * <p>链路：Filter 生成并写入 MDC → 业务日志自动携带 → 异常处理器回填到响应体与响应头。
 */
public final class TraceId {

    public static final String KEY = "traceId";
    public static final String HEADER = "X-Trace-Id";

    private TraceId() {
    }

    /** 生成一个新的 traceId（短 UUID，便于人工口述）。 */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /** 获取当前线程上下文中的 traceId，缺失时返回 "-"。 */
    public static String current() {
        String v = MDC.get(KEY);
        return (v == null || v.isEmpty()) ? "-" : v;
    }

    public static void set(String value) {
        MDC.put(KEY, value);
    }

    public static void clear() {
        MDC.remove(KEY);
    }
}
