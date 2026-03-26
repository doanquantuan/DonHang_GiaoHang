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

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
