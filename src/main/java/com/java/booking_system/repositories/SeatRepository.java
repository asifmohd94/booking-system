package com.java.booking_system.repositories;

import com.java.booking_system.entities.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.trainSchedule.id = :scheduleId AND s.travelClass.classCode = :classCode AND s.isBooked = false ORDER BY s.carriageNumber ASC, s.seatNumber ASC")
    List<Seat> findAvailableSeatsForUpdate(@Param("scheduleId") Long scheduleId, @Param("classCode") String classCode);

    long countByTrainScheduleIdAndTravelClassClassCodeAndIsBookedFalse(Long scheduleId, String classCode);

    List<Seat> findByTrainScheduleIdAndTravelClassClassCode(Long scheduleId, String classCode);

    List<Seat> findByTrainScheduleId(Long scheduleId);
}
