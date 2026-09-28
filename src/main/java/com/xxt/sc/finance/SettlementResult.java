package com.xxt.sc.finance;

/**
 * 清分结果（单位：分）。不变量：平台 + 楼长 + 供应商 + 手续费 == 订单行实付。
 */
public final class SettlementResult {

    private final long platformFen;
    private final long leaderFen;
    private final long supplierFen;
    private final long feeFen;
    private final String ruleVersion;

    SettlementResult(long platformFen, long leaderFen, long supplierFen, long feeFen, String ruleVersion) {
        this.platformFen = platformFen;
        this.leaderFen = leaderFen;
        this.supplierFen = supplierFen;
        this.feeFen = feeFen;
        this.ruleVersion = ruleVersion;
    }

    public long getPlatformFen() {
        return platformFen;
    }

    public long getLeaderFen() {
        return leaderFen;
    }

    public long getSupplierFen() {
        return supplierFen;
    }

    public long getFeeFen() {
        return feeFen;
    }

    public String getRuleVersion() {
        return ruleVersion;
    }

    /** 四方合计，应等于订单行实付。 */
    public long totalFen() {
        return platformFen + leaderFen + supplierFen + feeFen;
    }
}
