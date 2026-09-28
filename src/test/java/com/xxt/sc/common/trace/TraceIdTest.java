package com.xxt.sc.common.trace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** traceId 清洗测试：外部传入值必须被限制在安全字符集内，防止日志注入。 */
class TraceIdTest {

    @Test
    void 合法值原样保留() {
        assertEquals("abc123-XYZ_9", TraceId.sanitize("abc123-XYZ_9"));
    }

    @Test
    void 拒绝空值与空白() {
        assertNull(TraceId.sanitize(null));
        assertNull(TraceId.sanitize(""));
        assertNull(TraceId.sanitize("   "));
    }

    @Test
    void 拒绝换行与控制字符防止日志注入() {
        assertNull(TraceId.sanitize("abc\nERROR fake log line"));
        assertNull(TraceId.sanitize("abc\r\nX-Injected: 1"));
        assertNull(TraceId.sanitize("abc\tdef"));
    }

    @Test
    void 拒绝特殊字符与中文() {
        assertNull(TraceId.sanitize("abc def"));
        assertNull(TraceId.sanitize("abc@#$%"));
        assertNull(TraceId.sanitize("追踪标识"));
    }

    @Test
    void 拒绝超长值() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append('a');
        }
        assertNull(TraceId.sanitize(sb.toString()));
    }

    @Test
    void 生成值自身合法() {
        String generated = TraceId.generate();
        assertEquals(generated, TraceId.sanitize(generated));
    }
}
