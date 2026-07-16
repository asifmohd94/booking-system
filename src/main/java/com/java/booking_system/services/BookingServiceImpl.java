package com.java.booking_system.services;

import com.java.booking_system.dtos.BookingRequestDto;
import com.java.booking_system.dtos.BookingResponseDto;
import com.java.booking_system.entities.*;
import com.java.booking_system.exceptions.ResourceNotFoundException;
import com.java.booking_system.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final TravelClassRepository travelClassRepository;
    private final TrainStopRepository trainStopRepository;
    private final TrainScheduleRepository trainScheduleRepository;
    private final TrainClassConfigRepository trainClassConfigRepository;
    private final SeatRepository seatRepository;

    private final Random random = new Random();

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto request) {
        // 1. Validate request and entities
        Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new ResourceNotFoundException("Train not found with id: " + request.getTrainId()));

        Station sourceStation = stationRepository.findByStationCode(request.getSourceStationCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Source station not found with code: " + request.getSourceStationCode()));

        Station destinationStation = stationRepository.findByStationCode(request.getDestinationStationCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destination station not found with code: " + request.getDestinationStationCode()));

        TravelClass travelClass = travelClassRepository.findByClassCode(request.getTravelClassCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Travel class not found with code: " + request.getTravelClassCode()));

        // Check if train route is valid
        TrainStop srcStop = trainStopRepository.findByTrainIdAndStationId(train.getId(), sourceStation.getId())
                .orElseThrow(() -> new IllegalArgumentException("Source station " + request.getSourceStationCode()
                        + " is not a stop on train route " + train.getTrainNumber()));

        TrainStop destStop = trainStopRepository.findByTrainIdAndStationId(train.getId(), destinationStation.getId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Destination station " + request.getDestinationStationCode()
                                + " is not a stop on train route " + train.getTrainNumber()));

        if (srcStop.getStopSequence() >= destStop.getStopSequence()) {
            throw new IllegalArgumentException("Destination station stop must be after source station stop.");
        }

        // Get matching train schedule. boarding date is journeyDate, so origin
        // departure date is journeyDate - dayOffset
        LocalDate originDepartureDate = request.getJourneyDate().minusDays(srcStop.getDayOffset());
        TrainSchedule schedule = trainScheduleRepository
                .findByTrainIdAndDepartureDate(train.getId(), originDepartureDate)
                .orElseThrow(() -> new ResourceNotFoundException("No train schedule running for train "
                        + train.getTrainNumber() + " with origin departure on " + originDepartureDate));

        // Get class config for fare calculation
        TrainClassConfig classConfig = trainClassConfigRepository
                .findByTrainIdAndTravelClassClassCode(train.getId(), travelClass.getClassCode())
                .orElseThrow(() -> new ResourceNotFoundException("Travel class " + travelClass.getClassCode()
                        + " not configured for train " + train.getTrainNumber()));

        int requestedSeatsCount = request.getPassengers().size();
        if (requestedSeatsCount <= 0) {
            throw new IllegalArgumentException("At least one passenger is required for booking.");
        }

        // 2. Select seats with pessimistic write lock to satisfy concurrency criteria
        List<Seat> availableSeats = seatRepository.findAvailableSeatsForUpdate(schedule.getId(),
                travelClass.getClassCode());
        if (availableSeats.size() < requestedSeatsCount) {
            throw new IllegalArgumentException("Seat no longer available. Requested: " + requestedSeatsCount
                    + ", Available: " + availableSeats.size());
        }

        // 3. Allocate seats
        List<Seat> allocatedSeats = new ArrayList<>();
        for (int i = 0; i < requestedSeatsCount; i++) {
            Seat seat = availableSeats.get(i);
            seat.setBooked(true);
            seatRepository.save(seat);
            allocatedSeats.add(seat);
        }

        // 4. Generate PNR
        String pnr = generateUniquePnr();

        // 5. Calculate total fare
        double distance = destStop.getDistanceFromSource() - srcStop.getDistanceFromSource();
        double fareCoefficient = Math.max(1.0, distance / 100.0);
        double singleFare = BigDecimal.valueOf(classConfig.getBaseFare() * fareCoefficient)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
        double totalFare = singleFare * requestedSeatsCount;

        // 6. Persist booking
        Booking booking = Booking.builder()
                .trainSchedule(schedule)
                .sourceStation(sourceStation)
                .destinationStation(destinationStation)
                .pnr(pnr)
                .bookingDate(LocalDateTime.now())
                .travelClass(travelClass)
                .totalFare(totalFare)
                .status("CONFIRMED")
                .userId(request.getUserId())
                .build();
        booking = bookingRepository.save(booking);

        // 7. Persist passengers
        List<Passenger> savedPassengers = new ArrayList<>();
        for (int i = 0; i < requestedSeatsCount; i++) {
            BookingRequestDto.PassengerRequestDto pReq = request.getPassengers().get(i);
            Seat seat = allocatedSeats.get(i);

            Passenger passenger = Passenger.builder()
                    .booking(booking)
                    .name(pReq.getName())
                    .age(pReq.getAge())
                    .gender(pReq.getGender())
                    .seat(seat)
                    .build();
            savedPassengers.add(passengerRepository.save(passenger));
        }

        log.info("Booking created: bookingId={}, pnr={}, userId={}, passengers={}",
                booking.getId(), pnr, request.getUserId(), requestedSeatsCount);
        return convertToDto(booking, savedPassengers);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingById(Long bookingId) {
        log.debug("Fetching booking by id: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        List<Passenger> passengers = passengerRepository.findByBookingId(bookingId);
        return convertToDto(booking, passengers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByUserId(Long userId) {
        log.debug("Fetching bookings for userId: {}", userId);
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(b -> convertToDto(b, passengerRepository.findByBookingId(b.getId())))
                .toList();
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new IllegalStateException("Booking is already cancelled.");
        }

        // Release seats
        List<Passenger> passengers = passengerRepository.findByBookingId(bookingId);
        for (Passenger passenger : passengers) {
            Seat seat = passenger.getSeat();
            seat.setBooked(false);
            seatRepository.save(seat);
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        log.info("Booking cancelled: bookingId={}, pnr={}", bookingId, booking.getPnr());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getBookingByPnr(String pnr) {
        log.debug("Fetching booking by pnr: {}", pnr);
        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with PNR: " + pnr));
        List<Passenger> passengers = passengerRepository.findByBookingId(booking.getId());
        return convertToDto(booking, passengers);
    }

    private String generateUniquePnr() {
        while (true) {
            // Generate a 10-digit numeric string
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                sb.append(random.nextInt(10));
            }
            String pnr = sb.toString();
            if (bookingRepository.findByPnr(pnr).isEmpty()) {
                return pnr;
            }
        }
    }

    private BookingResponseDto convertToDto(Booking booking, List<Passenger> passengers) {
        TrainSchedule schedule = booking.getTrainSchedule();
        Train train = schedule.getTrain();

        // Query stops to calculate actual boarding and arrival times
        TrainStop srcStop = trainStopRepository
                .findByTrainIdAndStationId(train.getId(), booking.getSourceStation().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Boarding station stop not found."));

        TrainStop destStop = trainStopRepository
                .findByTrainIdAndStationId(train.getId(), booking.getDestinationStation().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination station stop not found."));

        LocalDate boardingDate = schedule.getDepartureDate().plusDays(srcStop.getDayOffset());
        LocalDateTime departureTime = LocalDateTime.of(boardingDate, srcStop.getDepartureTime());

        LocalDate arrivalDate = schedule.getDepartureDate().plusDays(destStop.getDayOffset());
        LocalDateTime arrivalTime = LocalDateTime.of(arrivalDate, destStop.getArrivalTime());

        List<BookingResponseDto.PassengerResponseDto> passengerDtos = passengers.stream()
                .map(p -> BookingResponseDto.PassengerResponseDto.builder()
                        .name(p.getName())
                        .age(p.getAge())
                        .gender(p.getGender())
                        .carriageNumber(p.getSeat().getCarriageNumber())
                        .seatNumber(p.getSeat().getSeatNumber())
                        .berthType(p.getSeat().getBerthType().name())
                        .build())
                .toList();

        return BookingResponseDto.builder()
                .bookingId(booking.getId())
                .pnr(booking.getPnr())
                .trainNumber(train.getTrainNumber())
                .trainName(train.getTrainName())
                .sourceStationCode(booking.getSourceStation().getStationCode())
                .sourceStationName(booking.getSourceStation().getStationName())
                .destinationStationCode(booking.getDestinationStation().getStationCode())
                .destinationStationName(booking.getDestinationStation().getStationName())
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .travelClassCode(booking.getTravelClass().getClassCode())
                .totalFare(booking.getTotalFare())
                .bookingDate(booking.getBookingDate())
                .status(booking.getStatus())
                .userId(booking.getUserId())
                .passengers(passengerDtos)
                .build();
    }
}
