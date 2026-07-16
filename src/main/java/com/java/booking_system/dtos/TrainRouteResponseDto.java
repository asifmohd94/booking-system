package com.java.booking_system.dtos;

import lombok.*;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainRouteResponseDto {
    private String trainNumber;
    private String trainName;
    private List<StopDto> stops;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StopDto {
        private String stationCode;
        private String stationName;
        private Integer stopSequence;
        private LocalTime arrivalTime;
        private LocalTime departureTime;
        private Double distanceFromSource;
        private Integer dayOffset;
    }
}
