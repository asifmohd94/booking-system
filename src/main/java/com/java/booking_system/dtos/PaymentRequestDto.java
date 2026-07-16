package com.java.booking_system.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    @NotNull(message = "bookingId is required")
    @Positive(message = "bookingId must be a positive number")
    private Long bookingId;

    @Positive(message = "amount must be a positive number")
    private Double amount;
}
