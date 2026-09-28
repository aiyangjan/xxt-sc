package com.xxt.sc.order;

import com.xxt.sc.catalog.SkuPriceProvider;
import com.xxt.sc.common.exception.BizException;
import com.xxt.sc.common.result.ErrorCode;
import com.xxt.sc.order.api.OrderCreateRequest;
import com.xxt.sc.order.api.OrderLineResponse;
import com.xxt.sc.order.api.OrderResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * P0 订单用例服务。
 *
 * <p>当前使用内存仓储建立 API 与领域边界。P1 接入数据库仓储时，订单计价和状态规则不变。
 */
@Service
public class OrderApplicationService {

    private final SkuPriceProvider priceProvider;
    private final ConcurrentMap<String, StoredOrder> orders = new ConcurrentHashMap<>();

    public OrderApplicationService(SkuPriceProvider priceProvider) {
        this.priceProvider = priceProvider;
    }

    public OrderResponse create(OrderCreateRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "订单商品不能为空");
        }

        Set<Long> seenSkuIds = new HashSet<>();
        long[] subtotals = new long[request.getItems().size()];
        List<OrderItemSnapshot> snapshots = new ArrayList<>();

        for (int i = 0; i < request.getItems().size(); i++) {
            OrderCreateRequest.Item item = request.getItems().get(i);
            if (item == null || item.getSkuId() == null || item.getQuantity() == null
                    || item.getSkuId() <= 0 || item.getQuantity() <= 0) {
                throw new BizException(ErrorCode.PARAM_ERROR, "SKU 和购买数量必须为正数");
            }
            if (!seenSkuIds.add(item.getSkuId())) {
                throw new BizException(ErrorCode.PARAM_ERROR, "同一订单不能重复提交同一个 SKU");
            }

            long unitPriceFen = priceProvider.currentPriceFen(item.getSkuId())
                    .orElseThrow(() -> new BizException(ErrorCode.SKU_OFF_SHELF,
                            "SKU " + item.getSkuId() + " 不存在或已下架"));
            long subtotalFen;
            try {
                subtotalFen = Math.multiplyExact(unitPriceFen, item.getQuantity().longValue());
            } catch (ArithmeticException e) {
                throw new BizException(ErrorCode.PARAM_ERROR, "订单金额超出可计算范围", e);
            }
            subtotals[i] = subtotalFen;
            snapshots.add(new OrderItemSnapshot(item.getSkuId(), item.getQuantity(), unitPriceFen,
                    subtotalFen, 0L, 0L, 0L, 0L));
        }

        OrderAmount amount = OrderPricing.calculate(subtotals, 0L, 0L);
        List<OrderItemSnapshot> pricedSnapshots = new ArrayList<>(snapshots.size());
        long[] payable = amount.getItemPayable();
        for (int i = 0; i < snapshots.size(); i++) {
            OrderItemSnapshot source = snapshots.get(i);
            pricedSnapshots.add(new OrderItemSnapshot(source.getSkuId(), source.getQuantity(),
                    source.getUnitPriceFen(), source.getSubtotalFen(),
                    amount.getItemActivityCut()[i], amount.getItemCouponCut()[i], payable[i], 0L));
        }

        String orderNo = generateOrderNo();
        StoredOrder stored = new StoredOrder(orderNo, OrderStatus.PENDING_PAY, amount, pricedSnapshots);
        orders.put(orderNo, stored);
        return toResponse(stored);
    }

    public OrderResponse get(String orderNo) {
        return toResponse(getStored(orderNo));
    }

    public OrderResponse cancel(String orderNo) {
        StoredOrder stored = getStored(orderNo);
        synchronized (stored) {
            if (!OrderStatus.canTransit(stored.status, OrderStatus.CANCELLED)) {
                throw new BizException(ErrorCode.ORDER_NOT_CANCELABLE,
                        "订单当前状态为「" + stored.status.getLabel() + "」，不可取消");
            }
            stored.status = OrderStatus.CANCELLED;
            return toResponse(stored);
        }
    }

    private StoredOrder getStored(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "订单号不能为空");
        }
        StoredOrder stored = orders.get(orderNo);
        if (stored == null) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND, "订单不存在或无权查看");
        }
        return stored;
    }

    private OrderResponse toResponse(StoredOrder stored) {
        List<OrderLineResponse> lines = new ArrayList<>(stored.items.size());
        for (OrderItemSnapshot item : stored.items) {
            lines.add(new OrderLineResponse(item.getSkuId(), item.getQuantity(), item.getUnitPriceFen(),
                    item.getSubtotalFen(), item.getActivityCutFen(), item.getCouponCutFen(),
                    item.getPayableFen()));
        }
        return new OrderResponse(stored.orderNo, stored.status, stored.amount.getOriginalFen(),
                stored.amount.getActivityCutFen(), stored.amount.getCouponCutFen(),
                stored.amount.getPayableFen(), lines);
    }

    private String generateOrderNo() {
        return "XO" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }
`n
    private static final class StoredOrder {
        private final String orderNo;
        private final OrderAmount amount;
        private final List<OrderItemSnapshot> items;
        private volatile OrderStatus status;

        private StoredOrder(String orderNo, OrderStatus status, OrderAmount amount,
                            List<OrderItemSnapshot> items) {
            this.orderNo = orderNo;
            this.status = status;
            this.amount = amount;
            this.items = List.copyOf(items);
        }
    }
}