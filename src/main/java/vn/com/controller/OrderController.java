package vn.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import vn.com.dto.OrderDto;
import vn.com.entity.Delivery;
import vn.com.entity.Order;
import vn.com.entity.OrderDetail;
import vn.com.service.OrderService;

import java.util.*;
import java.util.stream.Collectors;

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
            return ResponseEntity.status(HttpStatus.CREATED).body(toMap(newOrder));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg("Lỗi khi tạo đơn hàng: " + e.getMessage()));
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        if (orders.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        // Force load delivery để tránh LazyLoading
        List<Map<String, Object>> result = orders.stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(toMap(order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(msg("Không tìm thấy đơn hàng với ID: " + id));
        }
    }

    // ── Convert Order → Map ──
    // Nhúng delivery.shipperName, shipperPhone, vehicleInfo vào response
    // để frontend có thể đọc trực tiếp qua data.delivery.shipperName
    private Map<String, Object> toMap(Order o) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",            o.getId());
        map.put("customerName",  o.getCustomerName());
        map.put("phone",         o.getPhone());
        map.put("email",         o.getEmail());
        map.put("address",       o.getAddress());
        map.put("status",        o.getStatus() != null ? o.getStatus().name() : "NEW");
        map.put("paymentMethod", o.getPaymentMethod());
        map.put("paymentStatus", o.getPaymentStatus());
        map.put("shippingFee",   o.getShippingFee());
        map.put("discount",      o.getDiscount());
        map.put("totalAmount",   o.getTotalAmount());
        map.put("orderDate",     o.getOrderDate());
        map.put("updatedAt",     o.getUpdatedAt());
        // Chi tiết sản phẩm
        List<Map<String, Object>> details = new ArrayList<>();
        if (o.getOrderDetails() != null) {
            for (OrderDetail d : o.getOrderDetails()) {
                Map<String, Object> dm = new LinkedHashMap<>();
                dm.put("id",          d.getId());
                dm.put("productName", d.getProductName());
                dm.put("quantity",    d.getQuantity());
                dm.put("price",       d.getPrice());
                details.add(dm);
            }
        }
        map.put("orderDetails", details);

        // Nhúng thông tin delivery — đây là nguồn chứa shipperName đúng
        Delivery delivery = o.getDelivery();
        if (delivery != null) {
            Map<String, Object> dm = new LinkedHashMap<>();
            dm.put("id",           delivery.getId());
            dm.put("shipperName",  delivery.getShipperName());   // ← frontend đọc từ đây
            dm.put("shipperPhone", delivery.getShipperPhone());
            dm.put("vehicleInfo",  delivery.getVehicleInfo());
            dm.put("expectedTime", delivery.getExpectedTime());
            dm.put("status",       delivery.getStatus() != null ? delivery.getStatus().name() : "WAITING");
            dm.put("note",         delivery.getNote());          // = deliveryNote
            dm.put("deliveryDate", delivery.getDeliveryDate());
            map.put("delivery", dm);

            // Alias ở top-level để tương thích frontend cũ dùng data.deliveryNote / data.expectedTime
            map.put("deliveryNote",  delivery.getNote());
            map.put("expectedTime",  delivery.getExpectedTime());
            map.put("shipperName",   delivery.getShipperName());
        } else {
            map.put("delivery",     null);
            map.put("deliveryNote", null);
            map.put("expectedTime", null);
            map.put("shipperName",  null);
        }

        return map;
    }

    private Map<String, String> msg(String message) {
        Map<String, String> m = new HashMap<>();
        m.put("message", message);
        return m;
    }
}
