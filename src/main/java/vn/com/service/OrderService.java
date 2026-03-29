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
        validateNonNegativeAmounts(dto);
        
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

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        // Force load delivery và orderDetails trong transaction để tránh LazyLoading
        orders.forEach(o -> {
            if (o.getDelivery() != null) o.getDelivery().getId();
            if (o.getOrderDetails() != null) o.getOrderDetails().size();
        });
        return orders;
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));
        // Force load delivery trong transaction để tránh LazyLoading
        if (order.getDelivery() != null) order.getDelivery().getId();
        if (order.getOrderDetails() != null) order.getOrderDetails().size();
        return order;
    }

    @Transactional
    public Order updateOrder(Long id, OrderDto dto) {
        validateNonNegativeAmounts(dto);
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

    private void validateNonNegativeAmounts(OrderDto dto) {
        if (dto.getShippingFee() != null && dto.getShippingFee() < 0) {
            throw new IllegalArgumentException("Phí giao hàng không được âm");
        }

        if (dto.getDiscount() != null && dto.getDiscount() < 0) {
            throw new IllegalArgumentException("Giảm giá không được âm");
        }

        if (dto.getOrderDetails() == null) {
            return;
        }

        for (OrderDto.OrderDetailDto detailDto : dto.getOrderDetails()) {
            if (detailDto.getPrice() != null && detailDto.getPrice() < 0) {
                throw new IllegalArgumentException("Đơn giá sản phẩm không được âm");
            }
        }
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        orderRepository.delete(order);
    }
}
