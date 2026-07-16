package com.java.booking_system.services;

import com.java.booking_system.dtos.*;
import java.time.LocalDate;
import java.util.List;

public interface TrainService {

    List<TrainSearchResponseDto> searchTrains(String sourceStation, String destinationStation, LocalDate journeyDate,
            String travelClass);

    TrainResponseDto getTrainById(Long id);

    TrainRouteResponseDto getTrainRoute(Long id);

    List<AvailabilityResponseDto> getTrainAvailability(Long id, LocalDate journeyDate);

}
