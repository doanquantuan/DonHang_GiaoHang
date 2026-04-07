package vn.com.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private String customerName;
    private String phone;
    private String email;
    private String address;

    private String status;
    private String paymentMethod;
    private String paymentStatus;

    private String deliveryNote;
    private String expectedTime;
    private String shipperName;

    private Double shippingFee;
    private Double discount;

    private List<OrderDetailDto> orderDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailDto {
        private String productName;
        private Integer quantity;
        private Double price;
    }
}