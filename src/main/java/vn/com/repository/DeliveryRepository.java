package vn.com.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.entity.Delivery;
import vn.com.enums.DeliveryStatus;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByStatus(DeliveryStatus status);
    Optional<Delivery> findByOrderId(Long orderId);
}
