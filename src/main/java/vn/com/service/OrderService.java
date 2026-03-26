package vn.com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.com.dto.OrderDto;
import vn.com.entity.Delivery;
import vn.com.entity.Order;
import vn.com.entity.OrderDetail;
import vn.com.enums.DeliveryStatus;
import vn.com.enums.OrderStatus;
import vn.com.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));
    }

    @Transactional
    public Order updateOrder(Long id, OrderDto dto) {
        Order order = getOrderById(id);
        
        order.setCustomerName(dto.getCustomerName());
        order.setPhone(dto.getPhone());
        order.setEmail(dto.getEmail());
        order.setAddress(dto.getAddress());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setPaymentStatus(dto.getPaymentStatus());
        order.setShippingFee(dto.getShippingFee() != null ? dto.getShippingFee() : 0.0);
        order.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : 0.0);

        if (dto.getOrderDetails() != null) {
            order.getOrderDetails().clear();
            double totalProductAmount = 0.0;
            for (OrderDto.OrderDetailDto detailDto : dto.getOrderDetails()) {
                OrderDetail detail = new OrderDetail();
                detail.setProductName(detailDto.getProductName());
                detail.setQuantity(detailDto.getQuantity());
                detail.setPrice(detailDto.getPrice());
                detail.setOrder(order);
                order.getOrderDetails().add(detail);
                totalProductAmount += (detail.getPrice() * detail.getQuantity());
            }
            order.setTotalAmount(totalProductAmount + order.getShippingFee() - order.getDiscount());
        }

        if (order.getDelivery() != null) {
            Delivery delivery = order.getDelivery();
            delivery.setNote(dto.getDeliveryNote());
            delivery.setExpectedTime(dto.getExpectedTime());
            delivery.setShipperName(dto.getShipperName());
        }

        return orderRepository.save(order);
    }
}
