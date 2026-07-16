package com.java.booking_system.services;

import com.java.booking_system.dtos.StationsResponseDto;

import java.util.List;

public interface StationService {
    List<StationsResponseDto> getAllStations();

    StationsResponseDto getStationById(Long id);

    List<StationsResponseDto> searchStations(String keyword);
}
