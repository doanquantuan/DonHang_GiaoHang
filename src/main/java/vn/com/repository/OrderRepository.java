package vn.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.com.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long>{

}
