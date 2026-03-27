package vn.com.dto;

import lombok.Data;
import vn.com.enums.DeliveryStatus;

public class DeliveryDto {

    @Data
    public static class CreateDeliveryRequest {
        private Long orderId;         
        private String shipperName;    
        private String shipperPhone;   
        private String vehicleInfo;    
        private String expectedTime;   
        private String note;           
    }

    @Data
    public static class UpdateStatusRequest {
        private DeliveryStatus status; 
        private String note;           
    }
}