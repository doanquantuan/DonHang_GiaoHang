package vn.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/deliveries")
public class DeliveryViewController {

    // OR-36: Danh sách giao hàng
    @GetMapping
    public String deliveryList() {
        return "deliveries/delivery-list"; // → templates/deliveries/delivery-list.html
    }

    // Tạo chuyến giao mới
    @GetMapping("/create")
    public String createDelivery() {
        return "deliveries/create-delivery"; // → templates/deliveries/create-delivery.html
    }
}