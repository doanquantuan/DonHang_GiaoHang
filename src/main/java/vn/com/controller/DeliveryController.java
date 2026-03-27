package vn.com.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.com.dto.DeliveryDto.CreateDeliveryRequest;
import vn.com.dto.DeliveryDto.UpdateStatusRequest;
import vn.com.entity.Delivery;
import vn.com.service.DeliveryService;

@RestController
@RequestMapping("/api/v1/deliveries")
public class DeliveryController {
    @Autowired
    private DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<?> createDelivery(@RequestBody CreateDeliveryRequest request) {
        try {
            Delivery newDelivery = deliveryService.createDelivery(request);
            return ResponseEntity.ok(newDelivery);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
