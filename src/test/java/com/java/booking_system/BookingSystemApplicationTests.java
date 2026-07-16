package com.java.booking_system;

import com.java.booking_system.dtos.BookingRequestDto;
import com.java.booking_system.dtos.BookingResponseDto;
import com.java.booking_system.entities.Seat;
import com.java.booking_system.entities.Train;
import com.java.booking_system.repositories.SeatRepository;
import com.java.booking_system.repositories.TrainRepository;
import com.java.booking_system.services.BookingService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookingSystemApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(BookingSystemApplicationTests.class);

	@Autowired
	private BookingService bookingService;

	@Autowired
	private TrainRepository trainRepository;

	@Autowired
	private SeatRepository seatRepository;

	@Autowired
	private com.java.booking_system.repositories.BookingRepository bookingRepository;

	@Autowired
	private com.java.booking_system.repositories.PassengerRepository passengerRepository;

	@Autowired
	private com.java.booking_system.repositories.PaymentRepository paymentRepository;

	@Autowired
	private com.java.booking_system.repositories.TrainScheduleRepository trainScheduleRepository;

	@Test
	void contextLoads() {
		// Verify context loads and database seeding occurred
		Optional<Train> train = trainRepository.findByTrainNumber("12430");
		assertTrue(train.isPresent(), "Train 12430 (Rajdhani) should have been seeded");
	}

	@Test
	void testConcurrentSeatBooking() throws InterruptedException {
		// 1. Reset database changes to allow test runs repeatedly on the same shared DB
		passengerRepository.deleteAll();
		paymentRepository.deleteAll();
		bookingRepository.deleteAll();

		List<Seat> allSeats = seatRepository.findAll();
		for (Seat s : allSeats) {
			s.setBooked(false);
		}
		seatRepository.saveAll(allSeats);

		// Train 12430 has 10 seats for class '3A' on 2026-07-20
		Long trainId = trainRepository.findByTrainNumber("12430")
				.orElseThrow()
				.getId();

		// We will trigger 6 bookings of 2 passengers each concurrently (total 12
		// seats).
		// Since there are only 10 seats, some should fail and some succeed.
		// No seat should be assigned to multiple passengers status-wise.
		int numberOfThreads = 6;
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		CountDownLatch latch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failureCount = new AtomicInteger(0);
		List<BookingResponseDto> successfulBookings = Collections.synchronizedList(new ArrayList<>());
		List<String> errorMessages = Collections.synchronizedList(new ArrayList<>());

		for (int i = 1; i <= numberOfThreads; i++) {
			final long userId = i;
			executorService.submit(() -> {
				try {
					latch.await(); // wait for start signal

					BookingRequestDto.PassengerRequestDto p1 = BookingRequestDto.PassengerRequestDto.builder()
							.name("Passenger A - User " + userId)
							.age(30)
							.gender("M")
							.build();

					BookingRequestDto.PassengerRequestDto p2 = BookingRequestDto.PassengerRequestDto.builder()
							.name("Passenger B - User " + userId)
							.age(28)
							.gender("F")
							.build();

					BookingRequestDto request = BookingRequestDto.builder()
							.trainId(trainId)
							.sourceStationCode("NDLS")
							.destinationStationCode("CSMT")
							.journeyDate(LocalDate.of(2026, 7, 20))
							.travelClassCode("3A")
							.userId(userId)
							.passengers(List.of(p1, p2))
							.build();

					BookingResponseDto response = bookingService.createBooking(request);
					successCount.incrementAndGet();
					successfulBookings.add(response);
				} catch (Exception e) {
					failureCount.incrementAndGet();
					errorMessages.add(e.getMessage());
				} finally {
					doneLatch.countDown();
				}
			});
		}

		latch.countDown(); // start all threads together
		doneLatch.await(); // wait for all threads to finish

		executorService.shutdown();

		log.info("Successful bookings: {}", successCount.get());
		log.info("Failed bookings: {}", failureCount.get());
		log.info("Error messages: {}", errorMessages);

		// Assert that exactly 5 bookings succeeded (each booked 2 seats, total 10
		// seats) and 1 failed
		assertEquals(5, successCount.get(), "Exactly 5 bookings should succeed because capacity is 10");
		assertEquals(1, failureCount.get(), "Exactly 1 booking should fail because capacity is 10");
		assertTrue(errorMessages.get(0).contains("Seat no longer available"),
				"Failure reason should be seat availability");

		// Double verify in the DB that exactly 10 seats are booked, and none are
		// duplicate
		// Retrieve all seats for class 3A on schedule for 2026-07-20
		com.java.booking_system.entities.TrainSchedule schedule = trainScheduleRepository
				.findByTrainIdAndDepartureDate(trainId, LocalDate.of(2026, 7, 20)).orElseThrow();
		List<Seat> seats = seatRepository.findByTrainScheduleIdAndTravelClassClassCode(schedule.getId(), "3A");

		long bookedCount = seats.stream().filter(Seat::isBooked).count();
		assertEquals(10, bookedCount, "Exactly 10 database seats must be flagged as booked");

		// Check if any seat is allocated to multiple passengers
		List<String> allocatedSeatsList = new ArrayList<>();
		for (BookingResponseDto booking : successfulBookings) {
			for (BookingResponseDto.PassengerResponseDto passenger : booking.getPassengers()) {
				String seatKey = passenger.getCarriageNumber() + "-" + passenger.getSeatNumber();
				assertFalse(allocatedSeatsList.contains(seatKey),
						"Seat " + seatKey + " was allocated to multiple passengers!");
				allocatedSeatsList.add(seatKey);
			}
		}
		assertEquals(10, allocatedSeatsList.size(), "Total unique seats allocated must be 10");
	}
}
