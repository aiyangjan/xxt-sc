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

    /** 允许的字符集：仅限字母、数字、短横线、下划线。空白与控制字符一律拒绝。 */
    private static final int MAX_LENGTH = 64;

    private TraceId() {
    }

    /** 生成一个新的 traceId（短 UUID，便于人工口述）。 */
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 清洗外部传入的 traceId。
     *
     * <p>traceId 来自请求头，属于不可信输入：若直接写入 MDC，可被用于
     * 伪造日志行（CRLF 注入）或撑爆日志字段。这里只允许有限字符集，
     * 不符合时返回 null，由调用方重新生成。
     *
     * @param raw 请求头中的原始值，可为 null
     * @return 安全的 traceId；不合法时返回 null
     */
    public static String sanitize(String raw) {
        if (raw == null) {
            return null;
        }
        String v = raw.trim();
        if (v.isEmpty() || v.length() > MAX_LENGTH) {
            return null;
        }
        for (int i = 0; i < v.length(); i++) {
            char c = v.charAt(i);
            boolean ok = (c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || c == '-' || c == '_';
            if (!ok) {
                return null;
            }
        }
        return v;
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
