package com.java.booking_system.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponseDto {
    private String classCode;
    private String className;
    private Integer availableSeats;
    private Double fare;
}
