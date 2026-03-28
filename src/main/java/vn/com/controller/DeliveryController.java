package vn.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.dto.DeliveryDto;
import vn.com.entity.Delivery;
import vn.com.entity.Order;
import vn.com.entity.User;
import vn.com.repository.UserRepository;
import vn.com.service.DeliveryService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin("*")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả delivery — trả về Map để tránh @JsonIgnore và LazyLoading
    @GetMapping
    public ResponseEntity<?> getAllDeliveries() {
        List<Delivery> list = deliveryService.getAllDeliveries();
        List<Map<String, Object>> result = list.stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // Tạo chuyến giao mới
    @PostMapping
    public ResponseEntity<?> createDelivery(@RequestBody DeliveryDto dto) {
        try {
            Delivery d = deliveryService.createDelivery(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(d));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg(e.getMessage()));
        }
    }

   

    // Xoá chuyến giao
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDelivery(@PathVariable Long id) {
        try {
            deliveryService.deleteDelivery(id);
            Map<String, String> res = new HashMap<>();
            res.put("message",    "Đã xoá chuyến giao thành công!");
            res.put("deliveryId", id.toString());
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg(e.getMessage()));
        }
    }

    // Lấy danh sách shipper (role = "SHIPPER", isActive = true)
    @GetMapping("/shippers")
    public ResponseEntity<?> getShippers() {
        List<User> shippers = userRepository.findByRoleAndIsActive("SHIPPER", true);
        List<Map<String, Object>> result = shippers.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",       u.getId());
            m.put("fullName", u.getFullName());
            m.put("username", u.getUsername());
            m.put("email",    u.getEmail());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ── Convert Delivery entity → Map ──
    // Tránh 2 vấn đề:
    //   1. @JsonIgnore trên Delivery.order → order.id không được serialize
    //   2. LazyLoading → NPE khi access order ngoài transaction
    private Map<String, Object> toMap(Delivery d) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",           d.getId());
        map.put("shipperName",  d.getShipperName());
        map.put("shipperPhone", d.getShipperPhone());
        map.put("vehicleInfo",  d.getVehicleInfo());
        map.put("expectedTime", d.getExpectedTime());
        map.put("status",       d.getStatus() != null ? d.getStatus().name() : "WAITING");
        map.put("note",         d.getNote());

        // Nhúng thông tin order cần thiết — không serialize cả entity tránh vòng lặp vô tận
        Order order = d.getOrder();
        if (order != null) {
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("id",           order.getId());
            o.put("customerName", order.getCustomerName());
            o.put("phone",        order.getPhone());
            o.put("address",      order.getAddress());
            o.put("totalAmount",  order.getTotalAmount());
            o.put("paymentMethod",order.getPaymentMethod());
            o.put("status",       order.getStatus() != null ? order.getStatus().name() : "NEW");
            map.put("order", o);
        } else {
            map.put("order", null);
        }

        return map;
    }

    private Map<String, String> msg(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        return m;
    }
}