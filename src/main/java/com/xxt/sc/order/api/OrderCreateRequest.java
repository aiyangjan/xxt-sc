package com.xxt.sc.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** 学生端创建订单请求。只接受 SKU 与数量，不接受客户端价格。 */
public class OrderCreateRequest {

    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Item {

        @NotNull(message = "SKU 不能为空")
        @Positive(message = "SKU 必须为正数")
        private Long skuId;

        @NotNull(message = "购买数量不能为空")
        @Positive(message = "购买数量必须大于 0")
        @Max(value = 9999, message = "单个 SKU 数量不能超过 9999")
        private Integer quantity;

        public Long getSkuId() {
            return skuId;
        }

        public void setSkuId(Long skuId) {
            this.skuId = skuId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}