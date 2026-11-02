package com.vehiclerental.service;

import com.vehiclerental.dto.RegisterForm;
import com.vehiclerental.dto.VehicleForm;
import com.vehiclerental.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Shared setup for the service tests: an empty database before each test, and helpers to create data. */
abstract class ServiceTestBase {

    private static final AtomicInteger NEXT_NUMBER = new AtomicInteger();

    @Autowired
    BookingService bookingService;
    @Autowired
    VehicleService vehicleService;
    @Autowired
    AuthService authService;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    Clock clock;

    @BeforeEach
    void emptyTheDatabase() {
        // Children first, so that no foreign key is broken
        jdbc.update("DELETE FROM audit_logs");
        jdbc.update("DELETE FROM payments");
        jdbc.update("DELETE FROM invoices");
        jdbc.update("DELETE FROM bookings");
        jdbc.update("DELETE FROM vehicles");
        jdbc.update("DELETE FROM users");
    }

    /** The start of the current hour: always a whole hour, and never in the future. */
    LocalDateTime thisHour() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.HOURS);
    }

    /** The soonest time a rental can start. */
    LocalDateTime nextHour() {
        return thisHour().plusHours(1);
    }

    /** Registers a new customer and returns their id. */
    long newCustomer() {
        int number = NEXT_NUMBER.incrementAndGet();
        RegisterForm form = new RegisterForm();
        form.setFullName("Test Customer " + number);
        form.setEmail("customer" + number + "@example.test");
        form.setPhone("9876543210");
        form.setLicenseNo("DL-" + number);
        form.setPassword("Password@123");
        return authService.register(form).getId();
    }

    /** Adds a car costing 1000 a day or 150 an hour: deposit 5000, late time charged at 1.5 times the price. */
    long newCar() {
        int number = NEXT_NUMBER.incrementAndGet();
        VehicleForm form = new VehicleForm();
        form.setType(VehicleType.CAR);
        form.setBrand("Test");
        form.setModel("Car " + number);
        form.setModelYear(2024);
        form.setRegNumber("TEST-" + number);
        form.setFuelType("Petrol");
        form.setTransmission("Manual");
        form.setSeats(5);
        form.setDailyRate(new BigDecimal("1000.00"));
        form.setHourlyRate(new BigDecimal("150.00"));
        return vehicleService.create(form);
    }
}
