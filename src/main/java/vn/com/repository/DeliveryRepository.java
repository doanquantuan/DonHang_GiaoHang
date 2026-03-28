package vn.com.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.com.entity.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long>{
    boolean existsByOrder_Id(Long orderId);
    Optional<Delivery> findByOrder_Id(Long orderId); 
}
