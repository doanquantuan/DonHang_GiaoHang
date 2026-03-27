package vn.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.com.dto.DeliveryDto.CreateDeliveryRequest;
import vn.com.dto.DeliveryDto.UpdateStatusRequest;
import vn.com.entity.Delivery;
import vn.com.service.DeliveryService;

@RestController
public class DeliveryController {
    @Autowired
    private DeliveryService deliveryService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable("id") Long id, @RequestBody UpdateStatusRequest request) {
        try {
            Delivery updatedDelivery = deliveryService.updateDeliveryStatus(id, request);
            return ResponseEntity.ok(updatedDelivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
