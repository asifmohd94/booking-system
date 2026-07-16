package com.java.booking_system.services;

import com.java.booking_system.dtos.BookingRequestDto;
import com.java.booking_system.dtos.BookingResponseDto;
import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto request);

    BookingResponseDto getBookingById(Long bookingId);

    List<BookingResponseDto> getBookingsByUserId(Long userId);

    void cancelBooking(Long bookingId);

    BookingResponseDto getBookingByPnr(String pnr);

}
