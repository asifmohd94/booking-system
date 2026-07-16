package com.java.booking_system.repositories;

import com.java.booking_system.entities.TrainSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface TrainScheduleRepository extends JpaRepository<TrainSchedule, Long> {
    Optional<TrainSchedule> findByTrainIdAndDepartureDate(Long trainId, LocalDate departureDate);
}
