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
    @Transactional
    public Order createNewOrder(OrderDto dto) {
        
        Order order = new Order();
        order.setCustomerName(dto.getCustomerName());
        order.setPhone(dto.getPhone());
        order.setEmail(dto.getEmail());
        order.setAddress(dto.getAddress());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setPaymentStatus(dto.getPaymentStatus());
        
        order.setShippingFee(dto.getShippingFee() != null ? dto.getShippingFee() : 0.0);
        order.setDiscount(dto.getDiscount() != null ? dto.getDiscount() : 0.0);
        
        order.setStatus(OrderStatus.NEW);

        double totalProductAmount = 0.0;
        List<OrderDetail> orderDetails = new ArrayList<>();

        if (dto.getOrderDetails() != null && !dto.getOrderDetails().isEmpty()) {
            for (OrderDto.OrderDetailDto detailDto : dto.getOrderDetails()) {
                OrderDetail detail = new OrderDetail();
                detail.setProductName(detailDto.getProductName());
                detail.setQuantity(detailDto.getQuantity());
                detail.setPrice(detailDto.getPrice());
                
                detail.setOrder(order); 
                
                orderDetails.add(detail);
                totalProductAmount += (detail.getPrice() * detail.getQuantity());
            }
        }
        order.setOrderDetails(orderDetails);

        double finalTotal = totalProductAmount + order.getShippingFee() - order.getDiscount();
        order.setTotalAmount(finalTotal);

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setNote(dto.getDeliveryNote());
        delivery.setExpectedTime(dto.getExpectedTime());
        delivery.setShipperName(dto.getShipperName());
        delivery.setStatus(DeliveryStatus.WAITING);

        order.setDelivery(delivery);

        return orderRepository.save(order);
    }
}