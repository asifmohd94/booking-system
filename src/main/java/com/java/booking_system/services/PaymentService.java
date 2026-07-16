package com.java.booking_system.services;

import com.java.booking_system.dtos.PaymentRequestDto;
import com.java.booking_system.dtos.PaymentResponseDto;

public interface PaymentService {

    PaymentResponseDto processPayment(PaymentRequestDto request);

    PaymentResponseDto getPaymentById(String paymentId);

}
