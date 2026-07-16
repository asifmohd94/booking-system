package com.java.booking_system.bootstrap;

import com.java.booking_system.entities.*;
import com.java.booking_system.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements ApplicationRunner {

        private final TrainRepository trainRepository;
        private final StationRepository stationRepository;
        private final TravelClassRepository travelClassRepository;
        private final TrainStopRepository trainStopRepository;
        private final TrainScheduleRepository trainScheduleRepository;
        private final TrainClassConfigRepository trainClassConfigRepository;
        private final SeatRepository seatRepository;

        @Override
        @Transactional
        public void run(ApplicationArguments args) throws Exception {
                if (trainRepository.count() > 0) {
                        log.info("Database already seeded. Skipping initial seeding.");
                        return;
                }

                log.info("Starting database seeding...");

                // Retrieve existing lookup items
                List<Station> stations = stationRepository.findAll();
                List<TravelClass> travelClasses = travelClassRepository.findAll();

                if (stations.isEmpty() || travelClasses.isEmpty()) {
                        log.error("Missing master data: stations or travel classes are not present. Seeding skipped.");
                        return;
                }

                Station ndls = stations.stream().filter(s -> "NDLS".equals(s.getStationCode())).findFirst()
                                .orElse(stations.get(0));
                Station csmt = stations.stream().filter(s -> "CSMT".equals(s.getStationCode())).findFirst()
                                .orElse(stations.get(0));

                // 1. Create a Train (Rajdhani Express)
                Train rajdhani = Train.builder()
                                .trainNumber("12430")
                                .trainName("New Delhi - Mumbai Rajdhani Express")
                                .trainType(TrainType.RAJDHANI)
                                .build();
                rajdhani = trainRepository.save(rajdhani);

                // 2. Create Train Stops (NDLS -> CSMT)
                // NDLS: Departure 17:00, sequence 0, distance 0
                TrainStop stop1 = TrainStop.builder()
                                .train(rajdhani)
                                .station(ndls)
                                .stopSequence(0)
                                .arrivalTime(null)
                                .departureTime(LocalTime.of(17, 0))
                                .distanceFromSource(0.0)
                                .dayOffset(0)
                                .build();
                trainStopRepository.save(stop1);

                // CSMT: Arrival 08:30 next day, sequence 1, distance 1384 km
                TrainStop stop2 = TrainStop.builder()
                                .train(rajdhani)
                                .station(csmt)
                                .stopSequence(1)
                                .arrivalTime(LocalTime.of(8, 30))
                                .departureTime(null)
                                .distanceFromSource(1384.0)
                                .dayOffset(1)
                                .build();
                trainStopRepository.save(stop2);

                // 3. Create Class configs
                List<TrainClassConfig> classConfigs = new ArrayList<>();
                for (TravelClass tc : travelClasses) {
                        // Rajdhani only supports AC classes: 1A, 2A, 3A
                        if ("1A".equals(tc.getClassCode()) || "2A".equals(tc.getClassCode())
                                        || "3A".equals(tc.getClassCode())) {
                                double baseFare = "1A".equals(tc.getClassCode()) ? 3000.0
                                                : "2A".equals(tc.getClassCode()) ? 2000.0 : 1500.0;

                                TrainClassConfig config = TrainClassConfig.builder()
                                                .train(rajdhani)
                                                .travelClass(tc)
                                                .totalSeats(10) // 10 seats per class for testing
                                                .baseFare(baseFare)
                                                .build();
                                classConfigs.add(trainClassConfigRepository.save(config));
                        }
                }

                // 4. Generate schedules and seats for the next 30 days starting from 2026-07-16
                LocalDate startDate = LocalDate.of(2026, 7, 16);
                for (int day = 0; day < 30; day++) {
                        LocalDate departureDate = startDate.plusDays(day);

                        TrainSchedule schedule = TrainSchedule.builder()
                                        .train(rajdhani)
                                        .departureDate(departureDate)
                                        .status(ScheduleStatus.ON_TIME)
                                        .build();
                        schedule = trainScheduleRepository.save(schedule);

                        // Generate seats for this schedule based on config
                        for (TrainClassConfig config : classConfigs) {
                                String carriage = "1A".equals(config.getTravelClass().getClassCode()) ? "H1"
                                                : "2A".equals(config.getTravelClass().getClassCode()) ? "A1" : "B1";

                                for (int seatNum = 1; seatNum <= config.getTotalSeats(); seatNum++) {
                                        BerthType berthType = seatNum % 3 == 1 ? BerthType.LOWER
                                                        : seatNum % 3 == 2 ? BerthType.MIDDLE : BerthType.UPPER;

                                        Seat seat = Seat.builder()
                                                        .trainSchedule(schedule)
                                                        .travelClass(config.getTravelClass())
                                                        .carriageNumber(carriage)
                                                        .seatNumber(seatNum)
                                                        .berthType(berthType)
                                                        .isBooked(false)
                                                        .build();
                                        seatRepository.save(seat);
                                }
                        }
                }

                log.info("Database seeding successfully completed. Rajdhani Express and class bookings seeded.");
        }
}
