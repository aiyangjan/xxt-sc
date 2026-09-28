package com.xxt.sc.decorate;

import java.util.regex.Pattern;

/**
 * 富文本清洗（用于装修 RICH_TEXT 组件）。
 *
 * <p>小程序端用 {@code <rich-text>} 渲染，服务端必须先清洗，否则装修功能等于
 * 给了运营一个存储型 XSS 入口。
 *
 * <p><strong>实现说明</strong>：当前为无外部依赖的正则黑名单清洗，覆盖常见向量
 * （script/iframe 等危险标签、on* 事件属性、javascript:/vbscript:/data: 协议、HTML 注释绕过）。
 * 若后续允许引入依赖，建议改用 jsoup 的{@code 白名单}模式（{@code Jsoup.clean(html, safelist)}），
 * 白名单比黑名单更可靠。
 */
public final class DecorateRichTextSanitizer {

    /** 成对出现的危险标签及其内容整体移除。 */
    private static final Pattern DANGEROUS_PAIR = Pattern.compile(
            "(?is)<\\s*(script|iframe|object|embed|style|form|textarea|link|meta)[^>]*>.*?<\\s*/\\s*\\1\\s*>");

    /** 自闭合或单标签形式的危险标签。 */
    private static final Pattern DANGEROUS_SINGLE = Pattern.compile(
            "(?i)<\\s*(script|iframe|object|embed|style|form|input|button|link|meta)[^>]*/?\\s*>");

    /** 事件处理器属性，如 onclick / onerror / onload。 */
    private static final Pattern EVENT_ATTR = Pattern.compile(
            "(?i)\\son[a-z]+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)");

    /** 危险协议。 */
    private static final Pattern DANGEROUS_PROTOCOL = Pattern.compile(
            "(?i)\\b(javascript|vbscript|data)\\s*:");

    /** HTML 注释（常被用于绕过黑名单）。 */
    private static final Pattern COMMENT = Pattern.compile("(?s)<!--.*?-->");

    private DecorateRichTextSanitizer() {
    }

    /**
     * 清洗富文本。
     *
     * @param html 原始 HTML，可为 null
     * @return 清洗后的内容；输入为 null 时返回空串
     */
    public static String sanitize(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        String r = html;
        r = COMMENT.matcher(r).replaceAll("");
        r = DANGEROUS_PAIR.matcher(r).replaceAll("");
        r = DANGEROUS_SINGLE.matcher(r).replaceAll("");
        r = EVENT_ATTR.matcher(r).replaceAll("");
        r = DANGEROUS_PROTOCOL.matcher(r).replaceAll("");
        return r.trim();
    }

    /**
     * 是否包含需要清洗的危险内容（用于保存前提示，而不是静默改写）。
     */
    public static boolean isDangerous(String html) {
        if (html == null || html.isEmpty()) {
            return false;
        }
        return !html.equals(sanitize(html));
    }
}
