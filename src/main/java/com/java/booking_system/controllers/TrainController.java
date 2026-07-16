package com.java.booking_system.controllers;

import com.java.booking_system.dtos.*;
import com.java.booking_system.services.TrainService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trains")
@RequiredArgsConstructor
@Validated
public class TrainController {

    private final TrainService trainService;

    @GetMapping("/search")
    public List<TrainSearchResponseDto> searchTrains(
            @RequestParam @NotBlank(message = "sourceStation is required") String sourceStation,
            @RequestParam @NotBlank(message = "destinationStation is required") String destinationStation,
            @RequestParam @NotNull(message = "journeyDate is required") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate journeyDate,
            @RequestParam @NotBlank(message = "travelClass is required") String travelClass) {
        return trainService.searchTrains(sourceStation, destinationStation, journeyDate, travelClass);
    }

    @GetMapping("/{id}")
    public TrainResponseDto getTrainById(@PathVariable Long id) {
        return trainService.getTrainById(id);
    }

    @GetMapping("/{id}/route")
    public TrainRouteResponseDto getTrainRoute(@PathVariable Long id) {
        return trainService.getTrainRoute(id);
    }

    @GetMapping("/{id}/availability")
    public List<AvailabilityResponseDto> getTrainAvailability(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "journeyDate is required") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate journeyDate) {
        return trainService.getTrainAvailability(id, journeyDate);
    }
}
