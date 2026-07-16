package com.java.booking_system.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {

    @NotNull(message = "trainId is required")
    @Positive(message = "trainId must be a positive number")
    private Long trainId;

    @NotBlank(message = "sourceStationCode is required")
    private String sourceStationCode;

    @NotBlank(message = "destinationStationCode is required")
    private String destinationStationCode;

    @NotNull(message = "journeyDate is required")
    private LocalDate journeyDate;

    @NotBlank(message = "travelClassCode is required")
    private String travelClassCode;

    @NotNull(message = "userId is required")
    @Positive(message = "userId must be a positive number")
    private Long userId;

    @NotEmpty(message = "At least one passenger is required")
    @Valid
    private List<PassengerRequestDto> passengers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerRequestDto {

        @NotBlank(message = "passenger name is required")
        @Size(max = 100, message = "passenger name must not exceed 100 characters")
        private String name;

        @NotNull(message = "passenger age is required")
        @Positive(message = "passenger age must be a positive number")
        private Integer age;

        @NotBlank(message = "passenger gender is required")
        @Size(max = 10, message = "passenger gender must not exceed 10 characters")
        private String gender;
    }
}
