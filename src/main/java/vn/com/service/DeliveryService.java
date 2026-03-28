package vn.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.dto.DeliveryDto;
import vn.com.entity.Delivery;
import vn.com.entity.Order;
import vn.com.enums.DeliveryStatus;
import vn.com.enums.OrderStatus;
import vn.com.repository.DeliveryRepository;
import vn.com.repository.OrderRepository;

import java.util.List;
import java.util.Map;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

  

    @Transactional(readOnly = true)
    public Delivery getDeliveryById(Long id) {
        Delivery d = deliveryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến giao ID: " + id));
        // Force load order trong transaction để tránh LazyLoading ngoài @Transactional
        if (d.getOrder() != null) d.getOrder().getId();
        return d;
    }

    // ── TẠO CHUYẾN GIAO MỚI ──
    @Transactional
    public Delivery createDelivery(DeliveryDto dto) {
        if (dto.getOrderId() == null)
            throw new RuntimeException("Vui lòng chọn đơn hàng!");
        if (dto.getShipperName() == null || dto.getShipperName().isBlank())
            throw new RuntimeException("Vui lòng nhập tên tài xế!");
        if (dto.getShipperPhone() == null || dto.getShipperPhone().isBlank())
            throw new RuntimeException("Vui lòng nhập SĐT tài xế!");
        if (dto.getVehicleInfo() == null || dto.getVehicleInfo().isBlank())
            throw new RuntimeException("Vui lòng nhập biển số xe!");
        if (dto.getExpectedTime() == null || dto.getExpectedTime().isBlank())
            throw new RuntimeException("Vui lòng nhập thời gian giao dự kiến!");

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + dto.getOrderId()));

        // Nếu đã có delivery → cập nhật thay vì tạo mới
        Delivery delivery = order.getDelivery();
        if (delivery == null) {
            delivery = new Delivery();
            delivery.setOrder(order);
        }

        delivery.setShipperName(dto.getShipperName());
        delivery.setShipperPhone(dto.getShipperPhone());
        delivery.setVehicleInfo(dto.getVehicleInfo());
        delivery.setExpectedTime(dto.getExpectedTime());
        delivery.setNote(dto.getNote());

        // Parse delivery status từ String (mặc định WAITING)
        DeliveryStatus dStatus = parseDeliveryStatus(dto.getStatus(), DeliveryStatus.WAITING);
        delivery.setStatus(dStatus);

        // Đồng bộ OrderStatus
        order.setStatus(toOrderStatus(dStatus));
        orderRepository.save(order);

        return deliveryRepository.save(delivery);
    }

    // ── CẬP NHẬT TRẠNG THÁI ──
    // Gọi từ frontend khi đổi dropdown → cập nhật vào bảng Deliveries VÀ Orders
    @Transactional
    public Delivery updateDeliveryStatus(Long deliveryId, Map<String, String> body) {
        Delivery delivery = getDeliveryById(deliveryId);

        String newStatusStr = body.get("status");
        DeliveryStatus newStatus = parseDeliveryStatus(newStatusStr, delivery.getStatus());
        delivery.setStatus(newStatus);

        // Cập nhật deliveryDate nếu có (frontend gửi khi chuyển sang DONE)
        if (body.containsKey("deliveryDate") && body.get("deliveryDate") != null
                && !body.get("deliveryDate").isBlank()) {
            try {
                delivery.setDeliveryDate(
                    java.time.LocalDateTime.parse(
                        body.get("deliveryDate"),
                        java.time.format.DateTimeFormatter.ISO_DATE_TIME
                    )
                );
            } catch (Exception ignored) {
                // Nếu parse lỗi → ghi thời điểm hiện tại
                delivery.setDeliveryDate(java.time.LocalDateTime.now());
            }
        } else if (newStatus == DeliveryStatus.DONE && delivery.getDeliveryDate() == null) {
            // Fallback: nếu không có deliveryDate nhưng status là DONE → ghi now()
            delivery.setDeliveryDate(java.time.LocalDateTime.now());
        }

        // Cập nhật shipperName nếu có (dùng khi phân công từ modal)
        if (body.containsKey("shipperName") && body.get("shipperName") != null
                && !body.get("shipperName").isBlank()) {
            delivery.setShipperName(body.get("shipperName"));
        }
        // Cập nhật shipperPhone nếu có
        if (body.containsKey("shipperPhone") && body.get("shipperPhone") != null
                && !body.get("shipperPhone").isBlank()) {
            delivery.setShipperPhone(body.get("shipperPhone"));
        }
        // Cập nhật vehicleInfo nếu có
        if (body.containsKey("vehicleInfo") && body.get("vehicleInfo") != null
                && !body.get("vehicleInfo").isBlank()) {
            delivery.setVehicleInfo(body.get("vehicleInfo"));
        }

        // Đồng bộ OrderStatus
        Order order = delivery.getOrder();
        if (order != null) {
            order.setStatus(toOrderStatus(newStatus));
            // Khi giao thành công → tự động tích "Đã thanh toán"
            if (newStatus == DeliveryStatus.DONE) {
                order.setPaymentStatus("Đã thanh toán");
            }
            orderRepository.save(order);
        }

        return deliveryRepository.save(delivery);
    }

    // ── XOÁ CHUYẾN GIAO ──
    @Transactional
    public void deleteDelivery(Long id) {
        Delivery delivery = getDeliveryById(id);

        // Reset order về NEW khi xoá chuyến giao
        Order order = delivery.getOrder();
        if (order != null) {
            order.setStatus(OrderStatus.NEW);
            order.setDelivery(null);
            orderRepository.save(order);
        }
        deliveryRepository.delete(delivery);
    }

    // ── MAP DeliveryStatus → OrderStatus (dùng enum gốc) ──
    // DeliveryStatus: WAITING, DELIVERING, DONE, FAILED
    // OrderStatus   : NEW, CONFIRMED, COMPLETED, CANCELLED
    private OrderStatus toOrderStatus(DeliveryStatus ds) {
        return switch (ds) {
            case WAITING    -> OrderStatus.CONFIRMED;   // chờ lấy → đã xác nhận
            case DELIVERING -> OrderStatus.CONFIRMED;   // đang giao → vẫn confirmed
            case DONE       -> OrderStatus.COMPLETED;   // xong → hoàn thành
            case FAILED     -> OrderStatus.CANCELLED;   // thất bại → huỷ
        };
    }

    private DeliveryStatus parseDeliveryStatus(String s, DeliveryStatus fallback) {
        if (s == null || s.isBlank()) return fallback;
        try { return DeliveryStatus.valueOf(s.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return fallback; }
    }
}