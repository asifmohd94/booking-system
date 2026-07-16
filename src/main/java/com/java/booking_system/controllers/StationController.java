package com.java.booking_system.controllers;

import com.java.booking_system.dtos.StationsResponseDto;
import com.java.booking_system.services.StationService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
@Validated
public class StationController {

    private final StationService stationService;

    @GetMapping
    public List<StationsResponseDto> getAllStations() {
        return stationService.getAllStations();
    }

    @GetMapping("/{id}")
    public StationsResponseDto getStationById(@PathVariable Long id) {
        return stationService.getStationById(id);
    }

    @GetMapping("/search")
    public List<StationsResponseDto> searchStations(@RequestParam @NotBlank(message = "keyword is required") String keyword) {
        return stationService.searchStations(keyword);
    }
}
