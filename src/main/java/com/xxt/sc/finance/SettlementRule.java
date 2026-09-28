package com.xxt.sc.finance;

/**
 * 分润规则（不可变）。
 *
 * <p>PRD 4.5：楼长利润、平台利润、供应商应结金额分别计算，并明确
 * 优惠承担方、退款责任、手续费承担方与<strong>分润规则版本</strong>。
 *
 * <p>比例统一使用<strong>基点</strong>（basis point，1/10000）表达，
 * 避免浮点误差；规则带版本号，订单快照需记录所用版本，便于事后对账。
 */
public final class SettlementRule {

    private final String version;
    /** 平台分成基点（0~10000） */
    private final int platformRateBp;
    /** 楼长分成基点（0~10000） */
    private final int leaderRateBp;
    /** 手续费承担方 */
    private final FeeBearer feeBearer;

    public enum FeeBearer {
        /** 平台承担 */
        PLATFORM,
        /** 楼长承担 */
        LEADER,
        /** 平台与楼长各半（余数归平台） */
        SHARED
    }

    public SettlementRule(String version, int platformRateBp, int leaderRateBp, FeeBearer feeBearer) {
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("分润规则版本号必填");
        }
        if (platformRateBp < 0 || leaderRateBp < 0) {
            throw new IllegalArgumentException("分成基点不能为负");
        }
        if (platformRateBp + leaderRateBp > 10_000) {
            throw new IllegalArgumentException("平台与楼长分成之和不能超过 10000 基点");
        }
        if (feeBearer == null) {
            throw new IllegalArgumentException("手续费承担方必填");
        }
        this.version = version;
        this.platformRateBp = platformRateBp;
        this.leaderRateBp = leaderRateBp;
        this.feeBearer = feeBearer;
    }

    public String getVersion() {
        return version;
    }

    public int getPlatformRateBp() {
        return platformRateBp;
    }

    public int getLeaderRateBp() {
        return leaderRateBp;
    }

    public FeeBearer getFeeBearer() {
        return feeBearer;
    }
}
