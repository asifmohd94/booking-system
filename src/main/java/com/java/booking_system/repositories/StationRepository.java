package com.java.booking_system.repositories;

import com.java.booking_system.entities.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {

    Optional<Station> findByStationCode(String stationCode);

    List<Station> findByCityId(Long cityId);

    List<Station> findByStationNameContainingIgnoreCase(String stationName);

    @Query("SELECT s FROM Station s JOIN FETCH s.city")
    List<Station> findAllWithCity();

    @Query("SELECT s FROM Station s JOIN s.city c " +
            "WHERE LOWER(s.stationName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.stationCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.cityName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Station> searchStations(@Param("keyword") String keyword);

}
