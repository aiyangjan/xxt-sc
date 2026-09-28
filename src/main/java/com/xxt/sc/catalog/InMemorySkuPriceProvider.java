package com.xxt.sc.catalog;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.OptionalLong;

/**
 * P0 开发用价格适配器。
 *
 * <p>这些 SKU 仅用于接口联调，不能作为生产商品数据。P1 接入商品主数据和价格历史后替换。
 */
@Component
public class InMemorySkuPriceProvider implements SkuPriceProvider {

    private static final Map<Long, Long> PRICES = Map.of(
            10001L, 199L,
            10002L, 399L,
            10003L, 599L
    );

    @Override
    public OptionalLong currentPriceFen(long skuId) {
        Long price = PRICES.get(skuId);
        return price == null ? OptionalLong.empty() : OptionalLong.of(price);
    }
}