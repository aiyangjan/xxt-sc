package com.xxt.sc.decorate;

import com.xxt.sc.common.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 装修配置校验测试：任何非法配置都必须在服务端被拒绝。 */
class DecorateConfigValidatorTest {

    private static final String OK = "{"
            + "\"version\":1,"
            + "\"components\":["
            + "  {\"id\":\"c1\",\"code\":\"BANNER\",\"props\":{\"items\":["
            + "     {\"img\":\"https://oss/a.jpg\",\"link\":{\"type\":\"GOODS\",\"id\":1}}"
            + "  ]}}"
            + "]}";

    @Test
    void 合法配置通过() {
        assertDoesNotThrow(() ->
                DecorateConfigValidator.validate(OK, DecorateScene.PLATFORM_HOME));
    }

    @Test
    void 空配置被拒绝() {
        assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate(null, DecorateScene.PLATFORM_HOME));
        assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate("  ", DecorateScene.PLATFORM_HOME));
    }

    @Test
    void 非法JSON被拒绝() {
        assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate("{not json", DecorateScene.PLATFORM_HOME));
    }

    @Test
    void 缺少components被拒绝() {
        assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate("{\"version\":1}", DecorateScene.PLATFORM_HOME));
    }

    @Test
    void 未知组件code被拒绝() {
        String cfg = "{\"components\":[{\"id\":\"x\",\"code\":\"__EVIL__\"}]}";
        BizException e = assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate(cfg, DecorateScene.PLATFORM_HOME));
        assertTrue(e.getMessage().contains("不支持的装修组件"));
    }

    @Test
    void 组件与场景不匹配被拒绝() {
        // SHOP_CARD 只允许店铺首页
        String cfg = "{\"components\":[{\"id\":\"x\",\"code\":\"SHOP_CARD\"}]}";
        assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate(cfg, DecorateScene.PLATFORM_HOME));
        assertDoesNotThrow(() ->
                DecorateConfigValidator.validate(cfg, DecorateScene.SHOP_HOME));
    }

    @Test
    void 非法跳转类型被拒绝() {
        String cfg = "{\"components\":[{\"id\":\"x\",\"code\":\"IMAGE\","
                + "\"props\":{\"link\":{\"type\":\"http://evil.com\"}}}]}";
        BizException e = assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate(cfg, DecorateScene.PLATFORM_HOME));
        assertTrue(e.getMessage().contains("非法的跳转类型"));
    }

    @Test
    void 嵌套在数组中的非法跳转同样被拒绝() {
        String cfg = "{\"components\":[{\"id\":\"x\",\"code\":\"NAV_GRID\","
                + "\"props\":{\"items\":[{\"link\":{\"type\":\"BAD\"}},"
                + "{\"link\":{\"type\":\"GOODS\"}}]}}]}";
        assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate(cfg, DecorateScene.PLATFORM_HOME));
    }

    @Test
    void 富文本含脚本被拒绝() {
        String cfg = "{\"components\":[{\"id\":\"x\",\"code\":\"RICH_TEXT\","
                + "\"props\":{\"html\":\"<p>hi</p><script>alert(1)</script>\"}}]}";
        BizException e = assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate(cfg, DecorateScene.PLATFORM_HOME));
        assertTrue(e.getMessage().contains("脚本"));
    }

    @Test
    void 组件数量超限被拒绝() {
        StringBuilder sb = new StringBuilder("{\"components\":[");
        for (int i = 0; i < DecorateConfigValidator.MAX_COMPONENTS + 1; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"id\":\"c").append(i).append("\",\"code\":\"BLANK\"}");
        }
        sb.append("]}");
        assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate(sb.toString(), DecorateScene.PLATFORM_HOME));
    }

    @Test
    void 配置体积超限被拒绝() {
        StringBuilder sb = new StringBuilder("{\"components\":[{\"id\":\"x\","
                + "\"code\":\"RICH_TEXT\",\"props\":{\"html\":\"");
        for (int i = 0; i < DecorateConfigValidator.MAX_CONFIG_BYTES; i++) {
            sb.append('a');
        }
        sb.append("\"}}]}");
        assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate(sb.toString(), DecorateScene.PLATFORM_HOME));
    }

    @Test
    void 错误码为参数错误() {
        BizException e = assertThrows(BizException.class, () ->
                DecorateConfigValidator.validate("{\"a\":1}", DecorateScene.PLATFORM_HOME));
        assertEquals("40000", e.getErrorCode().getCode());
    }
}
