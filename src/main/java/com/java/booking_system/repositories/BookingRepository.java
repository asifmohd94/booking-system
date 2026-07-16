package com.java.booking_system.repositories;

import com.java.booking_system.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByPnr(String pnr);

    List<Booking> findByUserId(Long userId);
}
