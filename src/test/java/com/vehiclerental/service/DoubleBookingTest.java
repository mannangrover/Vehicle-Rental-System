package com.vehiclerental.service;

import com.vehiclerental.dto.VehicleSearchCriteria;
import com.vehiclerental.exception.VehicleNotAvailableException;
import com.vehiclerental.model.Booking;
import com.vehiclerental.model.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("h2")
@TestPropertySource(properties = {
        "app.seed-demo-data=false",
        "spring.datasource.url=jdbc:h2:mem:double_booking_test;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
class DoubleBookingTest extends ServiceTestBase {

    private static final int THREADS = 8;

    @Test
    void overlappingTimesAreRejected() {
        long carId = newCar();
        bookingService.createBooking(newCustomer(), carId, day(10), day(12));
        long otherCustomerId = newCustomer();

        assertRejected(otherCustomerId, carId, day(11), day(13)); // overlaps the end
        assertRejected(otherCustomerId, carId, day(8), day(11));  // overlaps the start
        assertRejected(otherCustomerId, carId, day(11), day(11).plusHours(2)); // inside
        assertRejected(otherCustomerId, carId, day(9), day(13));  // surrounds it
    }

    @Test
    void sharingASingleHourCountsAsAnOverlap() {
        long carId = newCar();
        bookingService.createBooking(newCustomer(), carId, day(10), day(12));
        long otherCustomerId = newCustomer();

        assertRejected(otherCustomerId, carId, day(8), day(10).plusHours(1));   // ends an hour after the other starts
        assertRejected(otherCustomerId, carId, day(12).minusHours(1), day(14)); // starts an hour before the other ends
    }

    @Test
    void aRentalCanStartWhenAnotherEnds() {
        long carId = newCar();
        bookingService.createBooking(newCustomer(), carId, day(10), day(10).plusHours(3));
        long otherCustomerId = newCustomer();

        Booking after = bookingService.createBooking(otherCustomerId, carId, day(10).plusHours(3), day(10).plusHours(5));
        Booking before = bookingService.createBooking(otherCustomerId, carId, day(10).minusHours(2), day(10));

        assertThat(after.getId()).isPositive();
        assertThat(before.getId()).isPositive();
    }

    @Test
    void nonOverlappingTimesAreAllowed() {
        long carId = newCar();
        bookingService.createBooking(newCustomer(), carId, day(10), day(12));
        long otherCustomerId = newCustomer();

        Booking before = bookingService.createBooking(otherCustomerId, carId, day(7), day(9));
        Booking after = bookingService.createBooking(otherCustomerId, carId, day(13), day(14));

        assertThat(before.getId()).isPositive();
        assertThat(after.getId()).isPositive();
    }

    @Test
    void aCancelledBookingFreesTheTimes() {
        long carId = newCar();
        long firstCustomerId = newCustomer();
        long secondCustomerId = newCustomer();
        Booking first = bookingService.createBooking(firstCustomerId, carId, day(10), day(12));
        assertRejected(secondCustomerId, carId, day(10), day(12));

        bookingService.cancel(first.getId(), firstCustomerId);

        Booking second = bookingService.createBooking(secondCustomerId, carId, day(10), day(12));
        assertThat(second.getId()).isNotEqualTo(first.getId());
    }

    @Test
    void searchHidesABookedVehicleOnlyForThoseTimes() {
        long bookedCarId = newCar();
        long freeCarId = newCar();
        bookingService.createBooking(newCustomer(), bookedCarId, day(10), day(12));

        assertThat(searchIds(day(10), day(12))).containsExactly(freeCarId);
        assertThat(searchIds(day(12).minusHours(1), day(14))).containsExactly(freeCarId); // shares the last hour
        assertThat(searchIds(day(12), day(14))).containsExactlyInAnyOrder(bookedCarId, freeCarId); // starts as it ends
        assertThat(searchIds(null, null)).containsExactlyInAnyOrder(bookedCarId, freeCarId);
    }

    @Test
    void onlyOneOfManySimultaneousBookingsSucceeds() throws Exception {
        long carId = newCar();
        LocalDateTime start = day(10);
        LocalDateTime end = day(12);
        List<Long> customerIds = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            customerIds.add(newCustomer());
        }

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch allReady = new CountDownLatch(THREADS);
        CountDownLatch go = new CountDownLatch(1);
        int successes = 0;
        int notAvailable = 0;
        try {
            List<Future<Booking>> results = new ArrayList<>();
            for (long customerId : customerIds) {
                results.add(pool.submit(() -> {
                    allReady.countDown();
                    go.await();
                    return bookingService.createBooking(customerId, carId, start, end);
                }));
            }

            assertThat(allReady.await(30, TimeUnit.SECONDS)).isTrue();
            go.countDown(); // every thread starts together

            for (Future<Booking> result : results) {
                try {
                    result.get(30, TimeUnit.SECONDS);
                    successes++;
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof VehicleNotAvailableException) {
                        notAvailable++;
                    } else {
                        fail("Expected VehicleNotAvailableException but got " + e.getCause(), e.getCause());
                    }
                }
            }
        } finally {
            pool.shutdownNow();
        }

        assertThat(successes).isEqualTo(1);
        assertThat(notAvailable).isEqualTo(THREADS - 1);
        Integer rows = jdbc.queryForObject("SELECT COUNT(*) FROM bookings WHERE vehicle_id = ?", Integer.class, carId);
        assertThat(rows).isEqualTo(1);
    }

    private LocalDateTime day(int daysFromNow) {
        return nextHour().plusDays(daysFromNow);
    }

    private void assertRejected(long customerId, long vehicleId, LocalDateTime start, LocalDateTime end) {
        assertThrows(VehicleNotAvailableException.class,
                () -> bookingService.createBooking(customerId, vehicleId, start, end));
    }

    private List<Long> searchIds(LocalDateTime start, LocalDateTime end) {
        VehicleSearchCriteria criteria = new VehicleSearchCriteria();
        criteria.setStartAt(start);
        criteria.setEndAt(end);
        List<Long> ids = new ArrayList<>();
        for (Vehicle vehicle : vehicleService.search(criteria)) {
            ids.add(vehicle.getId());
        }
        return ids;
    }
}
