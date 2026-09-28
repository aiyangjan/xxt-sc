package com.xxt.sc.order.api;

import com.xxt.sc.common.result.ApiResponse;
import com.xxt.sc.common.trace.TraceId;
import com.xxt.sc.order.OrderApplicationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 学生端 P0 订单接口。 */
@RestController
@RequestMapping("/api/v1/ma/orders")
public class OrderController {

    private final OrderApplicationService orderService;

    public OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.ok(orderService.create(request)).traceId(TraceId.current());
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderResponse> get(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.get(orderNo)).traceId(TraceId.current());
    }

    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<OrderResponse> cancel(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.cancel(orderNo)).traceId(TraceId.current());
    }
}