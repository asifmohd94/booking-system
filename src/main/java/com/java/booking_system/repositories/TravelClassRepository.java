package com.java.booking_system.repositories;

import com.java.booking_system.entities.TravelClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelClassRepository extends JpaRepository<TravelClass, Long> {

    Optional<TravelClass> findByClassCode(String classCode);

}