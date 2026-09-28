package com.xxt.sc.decorate;

/**
 * 装修页面场景。
 */
public enum DecorateScene {

    /** 平台首页（学生端小程序首页） */
    PLATFORM_HOME("平台首页"),

    /** 楼长店铺首页 */
    SHOP_HOME("店铺首页"),

    /** 运营活动页 */
    ACTIVITY("活动页");

    private final String label;

    DecorateScene(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
