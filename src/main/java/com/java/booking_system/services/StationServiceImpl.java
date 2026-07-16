package com.java.booking_system.services;

import com.java.booking_system.dtos.StationsResponseDto;
import com.java.booking_system.entities.Station;
import com.java.booking_system.repositories.StationRepository;
import com.java.booking_system.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationServiceImpl implements StationService {
    private final StationRepository stationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StationsResponseDto> getAllStations() {
        log.debug("Fetching all stations");
        List<Station> stations = stationRepository.findAllWithCity();

        return stations.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StationsResponseDto getStationById(Long id) {
        log.debug("Fetching station by id: {}", id);
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found with id: " + id));
        return convertToDto(station);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StationsResponseDto> searchStations(String keyword) {
        log.debug("Searching stations with keyword: {}", keyword);
        List<Station> stations = stationRepository.searchStations(keyword);
        return stations.stream()
                .map(this::convertToDto)
                .toList();
    }

    private StationsResponseDto convertToDto(Station station) {

        return StationsResponseDto.builder()
                .id(station.getId())
                .stationCode(station.getStationCode())
                .stationName(station.getStationName())
                .cityName(station.getCity().getCityName())
                .state(station.getCity().getState())
                .build();
    }
}
