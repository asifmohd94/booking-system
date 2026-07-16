package com.java.booking_system.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationsResponseDto {
    private Long id;
    private String stationCode;
    private String stationName;
    private String cityName;
    private String state;
}
