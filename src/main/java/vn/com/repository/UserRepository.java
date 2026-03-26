package vn.com.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.com.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
 // Lấy danh sách user theo role (VD: "SHIPPER")
    List<User> findByRole(String role);
 
    // Lấy shipper đang active
    List<User> findByRoleAndIsActive(String role, Boolean isActive);
}