package com.java.booking_system.controllers;

import com.java.booking_system.dtos.PaymentRequestDto;
import com.java.booking_system.dtos.PaymentResponseDto;
import com.java.booking_system.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDto processPayment(@Valid @RequestBody PaymentRequestDto request) {
        return paymentService.processPayment(request);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponseDto getPaymentById(@PathVariable String paymentId) {
        return paymentService.getPaymentById(paymentId);
    }
}
