package com.java.booking_system.services;

import com.java.booking_system.dtos.*;
import com.java.booking_system.entities.*;
import com.java.booking_system.exceptions.ResourceNotFoundException;
import com.java.booking_system.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainServiceImpl implements TrainService {

    private final TrainRepository trainRepository;
    private final TrainStopRepository trainStopRepository;
    private final TrainScheduleRepository trainScheduleRepository;
    private final TrainClassConfigRepository trainClassConfigRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TrainSearchResponseDto> searchTrains(String sourceStationCode, String destinationStationCode,
            LocalDate journeyDate, String travelClassCode) {
        log.debug("Searching trains from {} to {} on {} in class {}", sourceStationCode, destinationStationCode,
                journeyDate, travelClassCode);
        List<TrainStop> sourceStops = trainStopRepository.findByStationStationCode(sourceStationCode);
        List<TrainStop> destStops = trainStopRepository.findByStationStationCode(destinationStationCode);

        List<TrainSearchResponseDto> results = new ArrayList<>();

        for (TrainStop srcStop : sourceStops) {
            for (TrainStop destStop : destStops) {
                if (srcStop.getTrain().getId().equals(destStop.getTrain().getId())
                        && srcStop.getStopSequence() < destStop.getStopSequence()) {

                    Train train = srcStop.getTrain();

                    // Origin departure date = journeyDate - dayOffset of boarding station
                    LocalDate originDepartureDate = journeyDate.minusDays(srcStop.getDayOffset());

                    Optional<TrainSchedule> scheduleOpt = trainScheduleRepository
                            .findByTrainIdAndDepartureDate(train.getId(), originDepartureDate);

                    if (scheduleOpt.isPresent()) {
                        TrainSchedule schedule = scheduleOpt.get();

                        Optional<TrainClassConfig> configOpt = trainClassConfigRepository
                                .findByTrainIdAndTravelClassClassCode(train.getId(), travelClassCode);

                        if (configOpt.isPresent()) {
                            TrainClassConfig config = configOpt.get();

                            // Count available seats
                            long availableSeats = seatRepository
                                    .countByTrainScheduleIdAndTravelClassClassCodeAndIsBookedFalse(schedule.getId(),
                                            travelClassCode);

                            // Calculate times
                            LocalDateTime departureTime = LocalDateTime.of(journeyDate, srcStop.getDepartureTime());
                            LocalDate arrivalDate = journeyDate
                                    .plusDays(destStop.getDayOffset() - srcStop.getDayOffset());
                            LocalDateTime arrivalTime = LocalDateTime.of(arrivalDate, destStop.getArrivalTime());

                            // Calculate fare: baseFare * (distance / 100)
                            double distance = destStop.getDistanceFromSource() - srcStop.getDistanceFromSource();
                            double fareCoefficient = Math.max(1.0, distance / 100.0);
                            double totalFare = BigDecimal.valueOf(config.getBaseFare() * fareCoefficient)
                                    .setScale(2, RoundingMode.HALF_UP)
                                    .doubleValue();

                            results.add(TrainSearchResponseDto.builder()
                                    .id(train.getId())
                                    .trainNumber(train.getTrainNumber())
                                    .trainName(train.getTrainName())
                                    .departure(departureTime)
                                    .arrival(arrivalTime)
                                    .availableSeats((int) availableSeats)
                                    .fare(totalFare)
                                    .build());
                        }
                    }
                }
            }
        }
        log.debug("Train search returned {} results", results.size());
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public TrainResponseDto getTrainById(Long id) {
        log.debug("Fetching train by id: {}", id);
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with id: " + id));

        return TrainResponseDto.builder()
                .id(train.getId())
                .trainNumber(train.getTrainNumber())
                .trainName(train.getTrainName())
                .trainType(train.getTrainType().name())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TrainRouteResponseDto getTrainRoute(Long id) {
        log.debug("Fetching route for train id: {}", id);
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with id: " + id));

        List<TrainStop> stops = trainStopRepository.findByTrainIdOrderByStopSequenceAsc(id);

        List<TrainRouteResponseDto.StopDto> stopDtos = stops.stream()
                .map(stop -> TrainRouteResponseDto.StopDto.builder()
                        .stationCode(stop.getStation().getStationCode())
                        .stationName(stop.getStation().getStationName())
                        .stopSequence(stop.getStopSequence())
                        .arrivalTime(stop.getArrivalTime())
                        .departureTime(stop.getDepartureTime())
                        .distanceFromSource(stop.getDistanceFromSource())
                        .dayOffset(stop.getDayOffset())
                        .build())
                .toList();

        return TrainRouteResponseDto.builder()
                .trainNumber(train.getTrainNumber())
                .trainName(train.getTrainName())
                .stops(stopDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponseDto> getTrainAvailability(Long id, LocalDate journeyDate) {
        log.debug("Fetching availability for train id: {} on {}", id, journeyDate);
        trainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with id: " + id));

        TrainSchedule schedule = trainScheduleRepository.findByTrainIdAndDepartureDate(id, journeyDate)
                .orElseThrow(() -> new ResourceNotFoundException("Train schedule not departure on: " + journeyDate));

        List<TrainClassConfig> configs = trainClassConfigRepository.findByTrainId(id);
        List<AvailabilityResponseDto> availabilityList = new ArrayList<>();

        for (TrainClassConfig config : configs) {
            long availableSeats = seatRepository
                    .countByTrainScheduleIdAndTravelClassClassCodeAndIsBookedFalse(schedule.getId(),
                            config.getTravelClass().getClassCode());

            availabilityList.add(AvailabilityResponseDto.builder()
                    .classCode(config.getTravelClass().getClassCode())
                    .className(config.getTravelClass().getClassName())
                    .availableSeats((int) availableSeats)
                    .fare(config.getBaseFare())
                    .build());
        }

        return availabilityList;
    }
}
