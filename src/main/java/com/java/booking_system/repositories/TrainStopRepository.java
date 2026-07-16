package com.java.booking_system.repositories;

import com.java.booking_system.entities.TrainStop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainStopRepository extends JpaRepository<TrainStop, Long> {
    List<TrainStop> findByTrainIdOrderByStopSequenceAsc(Long trainId);

    List<TrainStop> findByStationStationCode(String stationCode);

    java.util.Optional<TrainStop> findByTrainIdAndStationId(Long trainId, Long stationId);
}
