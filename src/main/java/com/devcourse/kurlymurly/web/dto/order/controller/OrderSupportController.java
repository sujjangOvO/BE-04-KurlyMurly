package com.devcourse.kurlymurly.web.dto.order.controller;

import com.devcourse.kurlymurly.module.order.domain.OrderSupport;
import com.devcourse.kurlymurly.module.order.service.OrderSupportService;
import com.devcourse.kurlymurly.web.dto.order.OrderSupportCreate;
import com.devcourse.kurlymurly.web.dto.order.PageParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orderSupports")
public class OrderSupportController {
    private final OrderSupportService orderSupportService;

    public OrderSupportController(OrderSupportService orderSupportService) {
        this.orderSupportService = orderSupportService;
    }

    @PostMapping
    public OrderSupport takeOrderSupport(@RequestBody OrderSupportCreate.Request request) {
        return orderSupportService.takeOrderSupport(
                request.userId(),
                request.orderId(),
                request.category(),
                request.title(),
                request.content()
        );
    }

    @GetMapping
    public Page<OrderSupport> findOrderAll(@RequestBody PageParam param) {
        Pageable pageable = PageRequest.of(param.page(), param.size());

        return orderSupportService.findOrderSupport(pageable);
    }

    @GetMapping("/{id}")
    public OrderSupport findById(@PathVariable Long id) {
        return orderSupportService.findById(id);
    }

    @GetMapping("/{userId}")
    public List<OrderSupport> findAllByUserId(@PathVariable Long userId) {
        return orderSupportService.findAllByUserId(userId);
    }

    @PatchMapping("/{id}")
    public OrderSupport updateOrderSupport(@PathVariable Long id,
                                           @RequestBody OrderSupportCreate.UpdateRequest request) {
        return orderSupportService.updateOrderSupport(id, request.title(), request.content());
    }

    @PatchMapping("/prepare/{id}")
    public OrderSupport changeSupportToPrepare(@PathVariable Long id) {
        return orderSupportService.updateSupportToPrepare(id);
    }

    @PatchMapping("/start/{id}")
    public OrderSupport changeSupportToStart(@PathVariable Long id) {
        return orderSupportService.updateSupportToStart(id);
    }

    @PatchMapping("/done/{id}")
    public OrderSupport changeSupportToDone(@PathVariable Long id) {
        return orderSupportService.updateSupportToDone(id);
    }

    @DeleteMapping("/{id}")
    public void deleteOrderSupport(@PathVariable Long id) {
        orderSupportService.deleteOrderSupport(id);
    }
}
