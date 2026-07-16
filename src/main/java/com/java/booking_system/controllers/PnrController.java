package com.java.booking_system.controllers;

import com.java.booking_system.dtos.BookingResponseDto;
import com.java.booking_system.services.BookingService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pnr")
@RequiredArgsConstructor
@Validated
public class PnrController {

    private final BookingService bookingService;

    @GetMapping("/{pnr}")
    public BookingResponseDto getBookingByPnr(
            @PathVariable @NotBlank(message = "pnr is required") @Size(min = 10, max = 10, message = "pnr must be exactly 10 characters") String pnr) {
        return bookingService.getBookingByPnr(pnr);
    }
}
