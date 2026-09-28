package com.xxt.sc.decorate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxt.sc.common.exception.BizException;
import com.xxt.sc.common.result.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * 装修配置校验。
 *
 * <p>装修配置是运行时数据，会被小程序端直接执行渲染，因此服务端必须在保存前做完整校验：
 * 结构合法性、组件白名单、场景匹配、跳转类型白名单、富文本危险内容、体积与数量上限。
 *
 * <p>校验失败抛 {@link BizException}（{@link ErrorCode#PARAM_ERROR}），由
 * {@code GlobalExceptionHandler} 转成统一响应。
 */
public final class DecorateConfigValidator {

    /** 单份配置体积上限（字节）。 */
    public static final int MAX_CONFIG_BYTES = 256 * 1024;

    /** 单页组件数量上限。 */
    public static final int MAX_COMPONENTS = 50;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private DecorateConfigValidator() {
    }

    /**
     * 校验装修配置。
     *
     * @param configJson 配置 JSON
     * @param scene      目标场景
     * @throws BizException 任意一项不合法
     */
    public static void validate(String configJson, DecorateScene scene) {
        if (configJson == null || configJson.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "装修配置不能为空");
        }
        if (scene == null) {
            throw new BizException(ErrorCode.PARAM_ERROR, "装修场景不能为空");
        }

        int size = configJson.getBytes(StandardCharsets.UTF_8).length;
        if (size > MAX_CONFIG_BYTES) {
            throw new BizException(ErrorCode.PARAM_ERROR,
                    "装修配置过大，上限 " + MAX_CONFIG_BYTES / 1024 + "KB");
        }

        JsonNode root;
        try {
            root = MAPPER.readTree(configJson);
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_ERROR, "装修配置 JSON 格式非法");
        }
        if (root == null || !root.isObject()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "装修配置根节点必须是对象");
        }

        JsonNode components = root.get("components");
        if (components == null || !components.isArray()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "装修配置缺少 components 数组");
        }
        if (components.size() > MAX_COMPONENTS) {
            throw new BizException(ErrorCode.PARAM_ERROR,
                    "组件数量超出上限 " + MAX_COMPONENTS);
        }

        for (JsonNode comp : components) {
            validateComponent(comp, scene);
        }
    }

    private static void validateComponent(JsonNode comp, DecorateScene scene) {
        if (comp == null || !comp.isObject()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "组件节点必须是对象");
        }

        String code = comp.path("code").asText("");
        if (!DecorateComponentCode.isSupported(code)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "不支持的装修组件: " + code);
        }
        if (!DecorateComponentCode.supports(code, scene)) {
            throw new BizException(ErrorCode.PARAM_ERROR,
                    "组件 " + code + " 不支持场景 " + scene.name());
        }

        // 富文本必须无危险内容
        if (DecorateComponentCode.RICH_TEXT.name().equals(code)) {
            String html = comp.path("props").path("html").asText("");
            if (DecorateRichTextSanitizer.isDangerous(html)) {
                throw new BizException(ErrorCode.PARAM_ERROR,
                        "富文本包含不允许的脚本内容");
            }
        }

        // 递归校验所有 link 节点
        validateLinks(comp);
    }

    /**
     * 递归查找所有名为 link 的对象节点，校验其 type 在白名单内。
     */
    private static void validateLinks(JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            JsonNode link = node.get("link");
            if (link != null && link.isObject()) {
                String type = link.path("type").asText("");
                if (!LinkType.isValid(type)) {
                    throw new BizException(ErrorCode.PARAM_ERROR,
                            "非法的跳转类型: " + (type.isEmpty() ? "<空>" : type));
                }
            }
            Iterator<JsonNode> it = node.elements();
            while (it.hasNext()) {
                validateLinks(it.next());
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                validateLinks(child);
            }
        }
    }
}
