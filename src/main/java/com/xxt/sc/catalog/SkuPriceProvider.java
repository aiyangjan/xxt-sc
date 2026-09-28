package com.xxt.sc.catalog;

import java.util.OptionalLong;

/**
 * SKU 当前可售价格来源。
 *
 * <p>订单应用层只依赖这个边界，客户端不能提交价格。生产环境将替换为数据库商品与价格服务。
 */
public interface SkuPriceProvider {

    OptionalLong currentPriceFen(long skuId);
}