package com.xxt.sc.decorate;

/**
 * 装修配置生效范围。范围越小优先级越高（店铺 > 楼栋 > 学校 > 全局）。
 */
public enum DecorateScopeType {

    GLOBAL("全局", 0),
    SCHOOL("学校", 1),
    BUILDING("楼栋", 2),
    SHOP("店铺", 3);

    private final String label;
    private final int priority;

    DecorateScopeType(String label, int priority) {
        this.label = label;
        this.priority = priority;
    }

    public String getLabel() {
        return label;
    }

    /** 优先级，数值越大越优先命中。 */
    public int getPriority() {
        return priority;
    }
}
