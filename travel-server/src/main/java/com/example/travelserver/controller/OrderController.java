package com.example.travelserver.controller;

import com.example.travelserver.dto.user.OrderRequest;
import com.example.travelserver.service.user.OrderService;
import com.example.travelserver.vo.Result;
import com.example.travelserver.vo.user.OrderVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单接口（需登录）
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public Result<OrderVO> create(@RequestBody OrderRequest request) {
        return Result.ok(orderService.create(request));
    }

    @GetMapping("/list")
    public Result<List<OrderVO>> list() {
        return Result.ok(orderService.listMyOrders());
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return Result.ok();
    }
}
