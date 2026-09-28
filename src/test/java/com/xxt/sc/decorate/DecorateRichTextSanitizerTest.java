package com.xxt.sc.decorate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 富文本清洗测试：常见 XSS 向量必须被清除。 */
class DecorateRichTextSanitizerTest {

    @Test
    void 移除script标签() {
        assertEquals("<p>hi</p>",
                DecorateRichTextSanitizer.sanitize("<p>hi</p><script>alert(1)</script>"));
    }

    @Test
    void 移除iframe与object() {
        assertEquals("", DecorateRichTextSanitizer.sanitize("<iframe src=\"x\"></iframe>"));
        assertEquals("", DecorateRichTextSanitizer.sanitize("<object data=\"x\"></object>"));
    }

    @Test
    void 移除事件属性() {
        String r = DecorateRichTextSanitizer.sanitize("<img src=\"a.jpg\" onerror=\"alert(1)\">");
        assertFalse(r.contains("onerror"));
        assertTrue(r.contains("img"));
    }

    @Test
    void 移除javascript协议() {
        String r = DecorateRichTextSanitizer.sanitize("<a href=\"javascript:alert(1)\">x</a>");
        assertFalse(r.toLowerCase().contains("javascript:"));
    }

    @Test
    void 移除data协议() {
        String r = DecorateRichTextSanitizer.sanitize("<a href=\"data:text/html;base64,xxx\">x</a>");
        assertFalse(r.toLowerCase().contains("data:"));
    }

    @Test
    void 移除HTML注释() {
        assertFalse(DecorateRichTextSanitizer.sanitize("a<!-- hidden -->b").contains("hidden"));
    }

    @Test
    void 正常内容保留() {
        String ok = "<p><strong>今日推荐</strong><br>新鲜水果</p>";
        assertEquals(ok, DecorateRichTextSanitizer.sanitize(ok));
    }

    @Test
    void 空值安全() {
        assertEquals("", DecorateRichTextSanitizer.sanitize(null));
        assertEquals("", DecorateRichTextSanitizer.sanitize(""));
    }

    @Test
    void 危险判定() {
        assertFalse(DecorateRichTextSanitizer.isDangerous("<p>safe</p>"));
        assertTrue(DecorateRichTextSanitizer.isDangerous("<script>x</script>"));
        assertFalse(DecorateRichTextSanitizer.isDangerous(null));
    }
}
