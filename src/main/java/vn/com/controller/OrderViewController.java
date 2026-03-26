package vn.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
public class OrderViewController {

    @GetMapping
    public String orderList() {
        return "orders/order-list";      // → templates/orders/order-list.html
    }

    @GetMapping("/create")
    public String createOrder() {
        return "orders/create-order";   // → templates/orders/create-order.html
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id) {
        return "orders/order-detail";   // → templates/orders/order-detail.html
    }

    @GetMapping("/{id}/edit")
    public String editOrder(@PathVariable Long id) {
        return "orders/edit-order";     // → templates/orders/edit-order.html
    }
}