package com.java.booking_system.repositories;

import com.java.booking_system.entities.TrainClassConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TrainClassConfigRepository extends JpaRepository<TrainClassConfig, Long> {
    List<TrainClassConfig> findByTrainId(Long trainId);

    Optional<TrainClassConfig> findByTrainIdAndTravelClassClassCode(Long trainId, String classCode);
}
