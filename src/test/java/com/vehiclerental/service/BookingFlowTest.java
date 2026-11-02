package com.vehiclerental.service;

import com.vehiclerental.exception.AccessDeniedException;
import com.vehiclerental.exception.InvalidBookingException;
import com.vehiclerental.model.Booking;
import com.vehiclerental.model.BookingStatus;
import com.vehiclerental.model.Invoice;
import com.vehiclerental.model.Payment;
import com.vehiclerental.model.PaymentMethod;
import com.vehiclerental.model.PaymentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("h2")
@TestPropertySource(properties = {
        "app.seed-demo-data=false",
        "spring.datasource.url=jdbc:h2:mem:booking_flow_test;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
class BookingFlowTest extends ServiceTestBase {

    private long customerId;
    private long carId;

    @BeforeEach
    void createCustomerAndCar() {
        customerId = newCustomer();
        carId = newCar();
    }

    @Test
    void happyPathFromBookingToDepositRefund() {
        Booking booking = bookingService.createBooking(customerId, carId, nextHour(), nextHour().plusDays(1));
        long id = booking.getId();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getRentalAmount()).isEqualByComparingTo("1000.00");
        assertThat(booking.getDepositAmount()).isEqualByComparingTo("5000.00");

        assertThat(bookingService.getById(id).getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        Invoice invoice = bookingService.findInvoice(id).orElseThrow();
        assertThat(invoice.getInvoiceNumber()).isNotBlank();
        assertThat(invoice.getAmountDue()).isEqualByComparingTo("6000.00"); // rental + deposit

        Payment payment = bookingService.pay(id, customerId, PaymentMethod.UPI);
        assertThat(payment.getType()).isEqualTo(PaymentType.PAYMENT);
        assertThat(payment.getAmount()).isEqualByComparingTo("6000.00");
        assertThat(bookingService.findInvoice(id).orElseThrow().getAmountDue()).isEqualByComparingTo("0");

        bookingService.markPickedUp(id);
        Booking active = bookingService.getById(id);
        assertThat(active.getStatus()).isEqualTo(BookingStatus.ACTIVE);
        assertThat(active.getPickedUpAt()).isNotNull();

        bookingService.processReturn(id, LocalDateTime.now(clock), BigDecimal.ZERO);
        Booking completed = bookingService.getById(id);
        assertThat(completed.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(completed.getReturnedAt()).isNotNull();
        assertThat(completed.getLateFee()).isEqualByComparingTo("0");
        assertThat(completed.getDamageFee()).isEqualByComparingTo("0");

        assertThat(refundedAmount(id)).isEqualByComparingTo("5000.00"); // the deposit comes back
        Invoice finalInvoice = bookingService.findInvoice(id).orElseThrow();
        assertThat(finalInvoice.getTotalCharges()).isEqualByComparingTo("1000.00");
        assertThat(finalInvoice.getNetPaid()).isEqualByComparingTo("1000.00");
        assertThat(finalInvoice.getAmountDue()).isEqualByComparingTo("0");
    }

    @Test
    void lateReturnCoveredByTheDepositRefundsTheRest() {
        long id = threeDayRentalNowActive();
        // Ended 2 days ago: late fee 2 x 1500 = 3000. Paid 8000, charges 3000 + 3000.
        moveTimesIntoThePast(id, thisHour().minusDays(5), thisHour().minusDays(2));

        bookingService.processReturn(id, thisHour(), BigDecimal.ZERO);

        assertThat(bookingService.getById(id).getLateFee()).isEqualByComparingTo("3000.00");
        assertThat(refundedAmount(id)).isEqualByComparingTo("2000.00");
        Invoice invoice = bookingService.findInvoice(id).orElseThrow();
        assertThat(invoice.getNetPaid()).isEqualByComparingTo("6000.00");
        assertThat(invoice.getAmountDue()).isEqualByComparingTo("0");
    }

    @Test
    void lateReturnBeyondTheDepositLeavesABalanceTheCustomerCanPay() {
        long id = threeDayRentalNowActive();
        // Ended 6 days ago: late fee 6 x 1500 = 9000. Paid 8000, charges 3000 + 9000.
        moveTimesIntoThePast(id, thisHour().minusDays(9), thisHour().minusDays(6));

        bookingService.processReturn(id, thisHour(), BigDecimal.ZERO);

        assertThat(bookingService.getById(id).getLateFee()).isEqualByComparingTo("9000.00");
        assertThat(refundedAmount(id)).isEqualByComparingTo("0");
        assertThat(bookingService.findInvoice(id).orElseThrow().getAmountDue()).isEqualByComparingTo("4000.00");

        Payment balance = bookingService.pay(id, customerId, PaymentMethod.CARD);

        assertThat(balance.getAmount()).isEqualByComparingTo("4000.00");
        Invoice invoice = bookingService.findInvoice(id).orElseThrow();
        assertThat(invoice.getNetPaid()).isEqualByComparingTo("12000.00");
        assertThat(invoice.getAmountDue()).isEqualByComparingTo("0");
    }

    @Test
    void damageLargerThanTheDepositLeavesABalance() {
        long id = oneDayBooking();
        bookingService.pay(id, customerId, PaymentMethod.UPI);
        bookingService.markPickedUp(id);

        bookingService.processReturn(id, LocalDateTime.now(clock), new BigDecimal("6000.00"));

        Booking completed = bookingService.getById(id);
        assertThat(completed.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        assertThat(completed.getDamageFee()).isEqualByComparingTo("6000.00");
        assertThat(refundedAmount(id)).isEqualByComparingTo("0");
        // Charges 1000 + 6000, paid 6000
        assertThat(bookingService.findInvoice(id).orElseThrow().getAmountDue()).isEqualByComparingTo("1000.00");
    }

    @Test
    void cannotPickUpBeforePaying() {
        long id = oneDayBooking();
        assertThrows(InvalidBookingException.class, () -> bookingService.markPickedUp(id));
        assertThat(bookingService.getById(id).getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void cannotStartInThePast() {
        assertThrows(InvalidBookingException.class,
                () -> bookingService.createBooking(customerId, carId, thisHour().minusDays(1), nextHour()));
    }

    @Test
    void cannotEndBeforeTheStart() {
        assertThrows(InvalidBookingException.class,
                () -> bookingService.createBooking(customerId, carId, nextHour().plusDays(3), nextHour().plusDays(2)));
        assertThrows(InvalidBookingException.class,
                () -> bookingService.createBooking(customerId, carId, nextHour(), nextHour()));
    }

    @Test
    void timesMustBeWholeHours() {
        assertThrows(InvalidBookingException.class,
                () -> bookingService.createBooking(customerId, carId, nextHour().plusMinutes(30), nextHour().plusHours(2)));
        assertThrows(InvalidBookingException.class,
                () -> bookingService.createBooking(customerId, carId, nextHour(), nextHour().plusMinutes(90)));
    }

    @Test
    void anHourlyRentalIsPricedByTheHour() {
        Booking booking = bookingService.createBooking(customerId, carId, nextHour(), nextHour().plusHours(3));

        assertThat(booking.getHours()).isEqualTo(3);
        assertThat(booking.getDurationLabel()).isEqualTo("3 hours");
        assertThat(booking.getRentalAmount()).isEqualByComparingTo("450.00");
        assertThat(booking.getDepositAmount()).isEqualByComparingTo("5000.00");
    }

    @Test
    void lateReturnIsChargedPerHourAndPartHoursRoundUp() {
        long id = threeHourRentalNowActive();
        LocalDateTime returned = thisHour();
        // Ended 2 hours 30 minutes before the return, so 3 late hours at 225
        moveTimesIntoThePast(id, returned.minusHours(6), returned.minusMinutes(150));

        bookingService.processReturn(id, returned, BigDecimal.ZERO);

        assertThat(bookingService.getById(id).getLateFee()).isEqualByComparingTo("675.00");
        // Paid 5450, charges 450 + 675
        assertThat(refundedAmount(id)).isEqualByComparingTo("4325.00");
    }

    @Test
    void returningInsideTheRentalHasNoLateFee() {
        long id = threeHourRentalNowActive();

        bookingService.processReturn(id, LocalDateTime.now(clock), BigDecimal.ZERO);

        assertThat(bookingService.getById(id).getLateFee()).isEqualByComparingTo("0");
    }

    @Test
    void cannotRentForMoreThan30Days() {
        assertThrows(InvalidBookingException.class,
                () -> bookingService.createBooking(customerId, carId, nextHour(), nextHour().plusDays(30).plusHours(1)));

        Booking thirtyDays = bookingService.createBooking(customerId, carId, nextHour(), nextHour().plusDays(30));
        assertThat(thirtyDays.getHours()).isEqualTo(720);
    }

    @Test
    void customerCannotReadOrPaySomeoneElsesBooking() {
        long id = oneDayBooking();
        long otherCustomerId = newCustomer();

        assertThrows(AccessDeniedException.class, () -> bookingService.getForUser(id, otherCustomerId));
        assertThrows(AccessDeniedException.class, () -> bookingService.pay(id, otherCustomerId, PaymentMethod.CASH));

        assertThat(bookingService.listPayments(id)).isEmpty();
        assertThat(bookingService.getForUser(id, customerId).getId()).isEqualTo(id);
    }

    @Test
    void cancellingAfterPayingRefundsEverything() {
        long id = bookingService.createBooking(customerId, carId, nextHour().plusDays(3), nextHour().plusDays(6)).getId();
        bookingService.pay(id, customerId, PaymentMethod.UPI);

        bookingService.cancel(id, customerId);

        assertThat(bookingService.getById(id).getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(refundedAmount(id)).isEqualByComparingTo("8000.00"); // rental 3000 + deposit 5000
        Invoice invoice = bookingService.findInvoice(id).orElseThrow();
        assertThat(invoice.getNetPaid()).isEqualByComparingTo("0");
        assertThat(invoice.getAmountDue()).isEqualByComparingTo("0");
    }

    private long oneDayBooking() {
        return bookingService.createBooking(customerId, carId, nextHour(), nextHour().plusDays(1)).getId();
    }

    /** Picked up and paid in full. Rental 3000, deposit 5000. */
    private long threeDayRentalNowActive() {
        return nowActive(nextHour().plusDays(3));
    }

    /** Picked up and paid in full. Rental 450, deposit 5000. */
    private long threeHourRentalNowActive() {
        return nowActive(nextHour().plusHours(3));
    }

    private long nowActive(LocalDateTime end) {
        long id = bookingService.createBooking(customerId, carId, nextHour(), end).getId();
        bookingService.pay(id, customerId, PaymentMethod.UPI);
        bookingService.markPickedUp(id);
        return id;
    }

    /** A return can't be in the future, so a late return is set up by moving the rental into the past. */
    private void moveTimesIntoThePast(long bookingId, LocalDateTime start, LocalDateTime end) {
        jdbc.update("UPDATE bookings SET start_at = ?, end_at = ?, picked_up_at = ? WHERE id = ?",
                start, end, start, bookingId);
    }

    private BigDecimal refundedAmount(long bookingId) {
        BigDecimal total = BigDecimal.ZERO;
        for (Payment payment : bookingService.listPayments(bookingId)) {
            if (payment.getType() == PaymentType.REFUND) {
                total = total.add(payment.getAmount());
            }
        }
        return total;
    }
}
