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
    public Delivery updateDeliveryStatus(Long deliveryId, UpdateStatusRequest request) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chuyến giao hàng với ID: " + deliveryId));

        delivery.setStatus(request.getStatus());
        
        if (request.getNote() != null && !request.getNote().isEmpty()) {
            delivery.setNote(request.getNote());
        }

        if (request.getStatus() == DeliveryStatus.DONE) {
            delivery.setDeliveryDate(LocalDateTime.now());
        }

        return deliveryRepository.save(delivery);
    }
}
