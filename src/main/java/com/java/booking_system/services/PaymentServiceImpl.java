package com.java.booking_system.services;

import com.java.booking_system.dtos.PaymentRequestDto;
import com.java.booking_system.dtos.PaymentResponseDto;
import com.java.booking_system.entities.Booking;
import com.java.booking_system.entities.Payment;
import com.java.booking_system.exceptions.ResourceNotFoundException;
import com.java.booking_system.repositories.BookingRepository;
import com.java.booking_system.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public PaymentResponseDto processPayment(PaymentRequestDto request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Booking not found with id: " + request.getBookingId()));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new IllegalStateException("Cannot process payment for a cancelled booking.");
        }

        // Generate a random unique transaction ID
        String transactionId = "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        Payment payment = Payment.builder()
                .booking(booking)
                .paymentId(transactionId)
                .amount(request.getAmount() != null ? request.getAmount() : booking.getTotalFare())
                .paymentDate(LocalDateTime.now())
                .status("SUCCESS")
                .build();

        payment = paymentRepository.save(payment);

        log.info("Payment processed: paymentId={}, bookingId={}, amount={}",
                transactionId, booking.getId(), payment.getAmount());
        return convertToDto(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(String paymentId) {
        log.debug("Fetching payment by id: {}", paymentId);
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseGet(() -> {
                    try {
                        Long id = Long.parseLong(paymentId);
                        return paymentRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Payment not found with ID/transaction ID: " + paymentId));
                    } catch (NumberFormatException e) {
                        throw new ResourceNotFoundException("Payment not found with transaction ID: " + paymentId);
                    }
                });
        return convertToDto(payment);
    }

    private PaymentResponseDto convertToDto(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getPaymentId())
                .bookingId(payment.getBooking().getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .build();
    }
}
