package vn.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import vn.com.dto.OrderDto;
import vn.com.entity.Order;
import vn.com.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin("*")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderDto orderDto) {
        try {
            Order newOrder = orderService.createNewOrder(orderDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createMessage("Lỗi khi tạo đơn hàng: " + e.getMessage()));
        }
    }
    private Map<String, String> createMessage(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createMessage("Không tìm thấy đơn hàng với ID: " + id));
        }
    }

}
