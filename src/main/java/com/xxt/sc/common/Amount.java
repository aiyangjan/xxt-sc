package com.xxt.sc.common;

/**
 * 金额工具。
 *
 * <p>PRD 7.3：金额一律使用「分」为单位的长整型存储与传输，禁止使用浮点数参与金额计算，
 * 避免精度丢失导致对账、清分和手续费计算出现差异。
 */
public final class Amount {

    private Amount() {
    }

    /** 元转分（仅用于录入和展示层转换，入参必须是字符串以避免 double 精度问题）。 */
    public static long yuanToFen(String yuan) {
        if (yuan == null || yuan.trim().isEmpty()) {
            throw new IllegalArgumentException("金额不能为空");
        }
        return new java.math.BigDecimal(yuan.trim())
                .movePointRight(2)
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValueExact();
    }

    /** 分转元，仅用于展示。 */
    public static String fenToYuan(long fen) {
        return new java.math.BigDecimal(fen)
                .movePointLeft(2)
                .setScale(2, java.math.RoundingMode.UNNECESSARY)
                .toPlainString();
    }

    /** 按比例分摊（优惠/手续费分摊到订单行），余数摊到最后一以保证总额一致。 */
    public static long[] allocate(long totalFen, int[] weights) {
        long sum = 0;
        for (int w : weights) {
            sum += w;
        }
        if (sum == 0) {
            throw new IllegalArgumentException("分摊权重和不能为 0");
        }
        long[] result = new long[weights.length];
        long allocated = 0;
        for (int i = 0; i < weights.length - 1; i++) {
            long v = totalFen * weights[i] / sum;
            result[i] = v;
            allocated += v;
        }
        result[weights.length - 1] = totalFen - allocated;
        return result;
    }
}
