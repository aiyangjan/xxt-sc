package com.xxt.sc.decorate;

import java.util.Arrays;

/**
 * 装修组件跳转目标类型。
 *
 * <p>小程序端只能按白名单类型跳转，禁止任意 URL，避免钓鱼与越权跳转。
 */
public enum LinkType {

    NONE("不跳转"),
    GOODS("商品详情"),
    SHOP("店铺首页"),
    CATEGORY("分类列表"),
    ACTIVITY("活动页"),
    PAGE("内置页面");

    private final String label;

    LinkType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 是否为合法跳转类型（未知一律拒绝）。 */
    public static boolean isValid(String type) {
        if (type == null || type.isEmpty()) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(t -> t.name().equals(type));
    }
}
