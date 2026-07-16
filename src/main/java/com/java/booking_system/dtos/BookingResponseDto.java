package com.java.booking_system.dtos;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {

    private Long bookingId;
    private String pnr;
    private String trainNumber;
    private String trainName;
    private String sourceStationCode;
    private String sourceStationName;
    private String destinationStationCode;
    private String destinationStationName;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String travelClassCode;
    private Double totalFare;
    private LocalDateTime bookingDate;
    private String status;
    private Long userId;
    private List<PassengerResponseDto> passengers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerResponseDto {
        private String name;
        private Integer age;
        private String gender;
        private String carriageNumber;
        private Integer seatNumber;
        private String berthType;
    }
}
