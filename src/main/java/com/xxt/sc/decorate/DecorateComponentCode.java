package com.xxt.sc.decorate;

import java.util.Arrays;

/**
 * 装修组件白名单。
 *
 * <p>服务端只接受这里定义的 code，任何未知 code 一律拒绝——装修配置是运行时数据，
 * 若允许任意组件等于开放注入入口。
 */
public enum DecorateComponentCode {

    BANNER("轮播图", true, true, true),
    NAV_GRID("金刚区", true, true, true),
    NOTICE("公告栏", true, true, true),
    COUPON("优惠券", true, false, true),
    GOODS_FLOOR("商品楼层", true, true, true),
    SECKILL("限时特价", true, false, true),
    SHOP_CARD("店铺卡片", false, true, false),
    SHOP_NOTICE("店铺公告", false, true, false),
    IMAGE("单图", true, true, true),
    RICH_TEXT("富文本", true, true, true),
    BLANK("空白间隔", true, true, true),
    VIDEO("视频", true, true, true);

    private final String label;
    private final boolean platformHome;
    private final boolean shopHome;
    private final boolean activity;

    DecorateComponentCode(String label, boolean platformHome, boolean shopHome, boolean activity) {
        this.label = label;
        this.platformHome = platformHome;
        this.shopHome = shopHome;
        this.activity = activity;
    }

    public String getLabel() {
        return label;
    }

    /** code 是否在白名单内。 */
    public static boolean isSupported(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(c -> c.name().equals(code));
    }

    /** 该 code 是否允许用于指定场景。 */
    public static boolean supports(String code, DecorateScene scene) {
        if (scene == null || !isSupported(code)) {
            return false;
        }
        DecorateComponentCode c = valueOf(code);
        switch (scene) {
            case PLATFORM_HOME:
                return c.platformHome;
            case SHOP_HOME:
                return c.shopHome;
            case ACTIVITY:
                return c.activity;
            default:
                return false;
        }
    }
}
