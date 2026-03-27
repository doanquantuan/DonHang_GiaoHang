package vn.com.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.com.dto.DeliveryDto.CreateDeliveryRequest;
import vn.com.dto.DeliveryDto.UpdateStatusRequest;
import vn.com.entity.Delivery;
import vn.com.entity.Order;
import vn.com.enums.DeliveryStatus;
import vn.com.repository.DeliveryRepository;
import vn.com.repository.OrderRepository;

@Service
public class DeliveryService {
    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Delivery createDelivery(CreateDeliveryRequest request) {

        Delivery delivery = deliveryRepository.findByOrder_Id(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin giao hàng cho đơn hàng số " + request.getOrderId()));

        if (delivery.getShipperName() != null && !delivery.getShipperName().trim().isEmpty()) {
            throw new RuntimeException("Đơn hàng này đã được phân công cho tài xế khác rồi!");
        }

        delivery.setShipperName(request.getShipperName());
        delivery.setShipperPhone(request.getShipperPhone());
        delivery.setVehicleInfo(request.getVehicleInfo());
        delivery.setExpectedTime(request.getExpectedTime());
        
        if (request.getNote() != null && !request.getNote().isEmpty()) {
            delivery.setNote(request.getNote());
        }

        return deliveryRepository.save(delivery);
    }
}
