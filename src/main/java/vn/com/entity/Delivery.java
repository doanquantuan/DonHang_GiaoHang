package vn.com.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import vn.com.enums.DeliveryStatus;

@Entity
@Table(name = "Deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @JsonIgnore
    private Order order;

    @Column(name = "shipper_name", columnDefinition = "NVARCHAR(255)")
    private String shipperName;

    @Column(name = "shipper_phone", length = 20)
    private String shipperPhone;

    @Column(name = "vehicle_info", length = 100)
    private String vehicleInfo;

    @Column(name = "expected_time", length = 100)
    private String expectedTime;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status = DeliveryStatus.WAITING;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    private String note;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}