package com.java.booking_system.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainResponseDto {
    private Long id;
    private String trainNumber;
    private String trainName;
    private String trainType;
}
