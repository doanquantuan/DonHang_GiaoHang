package vn.com.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {
    private Long    orderId;       // bắt buộc - đơn hàng cần giao
    private String  shipperName;   // bắt buộc - tên tài xế
    private String  shipperPhone;  // SĐT tài xế
    private String  vehicleInfo;   // biển số xe
    private String  expectedTime;  // thời gian giao dự kiến
    private String  note;          // ghi chú
    private String  status;        // WAITING | DELIVERING | DONE | FAILED
}