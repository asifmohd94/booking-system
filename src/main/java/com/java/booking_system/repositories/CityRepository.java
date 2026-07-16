package com.java.booking_system.repositories;

import com.java.booking_system.entities.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    Optional<City> findByCityName(String cityName);

    boolean existsByCityName(String cityName);

}
