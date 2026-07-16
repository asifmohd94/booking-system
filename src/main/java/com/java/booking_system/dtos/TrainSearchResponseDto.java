package com.java.booking_system.dtos;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainSearchResponseDto {
    private Long id;
    private String trainNumber;
    private String trainName;
    private LocalDateTime departure;
    private LocalDateTime arrival;
    private Integer availableSeats;
    private Double fare;
}
