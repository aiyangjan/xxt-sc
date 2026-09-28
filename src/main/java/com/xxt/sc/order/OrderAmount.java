package com.xxt.sc.order;

/**
 * 订单金额计算结果（不可变值对象）。
 *
 * <p>PRD 4.1：客户端只提交 SKU 和数量，应付金额、优惠分摊、手续费、利润、分润
 * 全部由服务端计算并保存快照。本对象即那个快照的内存形态。
 *
 * <p>单位统一为「分」。
 */
public final class OrderAmount {

    private final long originalFen;
    private final long activityCutFen;
    private final long couponCutFen;
    private final long payableFen;

    /** 每个订单行分摊到的活动优惠，与入参行顺序一致。 */
    private final long[] itemActivityCut;
    /** 每个订单行分摊到的优惠券优惠。 */
    private final long[] itemCouponCut;
    /** 每个订单行实付金额（小计 - 活动优惠 - 券优惠）。 */
    private final long[] itemPayable;

    OrderAmount(long originalFen, long activityCutFen, long couponCutFen, long payableFen,
                long[] itemActivityCut, long[] itemCouponCut, long[] itemPayable) {
        this.originalFen = originalFen;
        this.activityCutFen = activityCutFen;
        this.couponCutFen = couponCutFen;
        this.payableFen = payableFen;
        this.itemActivityCut = itemActivityCut;
        this.itemCouponCut = itemCouponCut;
        this.itemPayable = itemPayable;
    }

    /** 原价合计（各行小计之和）。 */
    public long getOriginalFen() {
        return originalFen;
    }

    public long getActivityCutFen() {
        return activityCutFen;
    }

    public long getCouponCutFen() {
        return couponCutFen;
    }

    /** 应付金额 = 原价 - 活动优惠 - 券优惠。 */
    public long getPayableFen() {
        return payableFen;
    }

    public long[] getItemActivityCut() {
        return itemActivityCut.clone();
    }

    public long[] getItemCouponCut() {
        return itemCouponCut.clone();
    }

    public long[] getItemPayable() {
        return itemPayable.clone();
    }
}
