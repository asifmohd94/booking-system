package com.java.booking_system.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private String paymentId;
    private Long bookingId;
    private Double amount;
    private LocalDateTime paymentDate;
    private String status;
}
