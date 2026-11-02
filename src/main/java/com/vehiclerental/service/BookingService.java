package com.vehiclerental.service;

import com.vehiclerental.model.Booking;
import com.vehiclerental.model.BookingStatus;
import com.vehiclerental.model.Invoice;
import com.vehiclerental.model.Payment;
import com.vehiclerental.model.PaymentMethod;
import com.vehiclerental.model.RentalQuote;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Booking lifecycle: CONFIRMED (automatic when booked; invoice is created)
 * -> ACTIVE (paid and picked up) -> COMPLETED (returned).
 * CONFIRMED can become CANCELLED.
 * An illegal status change throws InvalidBookingException.
 * Rentals run in whole hours, and a rental can start at the moment another ends.
 */
public interface BookingService {

    /** @throws com.vehiclerental.exception.InvalidBookingException if the times break the rental policy */
    RentalQuote quote(long vehicleId, LocalDateTime startAt, LocalDateTime endAt);

    /**
     * Creates a CONFIRMED booking and publishes BookingConfirmedEvent, which creates the invoice. Safe against two customers booking the same vehicle at once.
     *
     * @throws com.vehiclerental.exception.VehicleNotAvailableException if the times overlap another booking
     *         or the vehicle is not AVAILABLE
     * @throws com.vehiclerental.exception.InvalidBookingException if the times break the rental policy
     */
    Booking createBooking(long userId, long vehicleId, LocalDateTime startAt, LocalDateTime endAt);

    /** @throws com.vehiclerental.exception.ResourceNotFoundException if there is no such booking */
    Booking getById(long bookingId);

    /** @throws com.vehiclerental.exception.AccessDeniedException if the booking belongs to someone else */
    Booking getForUser(long bookingId, long userId);

    List<Booking> listForUser(long userId);

    /** @param status filter, or null for every booking */
    List<Booking> listAll(BookingStatus status);

    /** Customer cancels their own CONFIRMED booking before its start time. All payments are refunded. */
    void cancel(long bookingId, long userId);

    /** Customer pays the full amount due (simulated). Allowed when CONFIRMED or COMPLETED and an amount is due. */
    Payment pay(long bookingId, long userId, PaymentMethod method);

    /** Admin: CONFIRMED to ACTIVE, from shortly before the start time, and only once paid in full. */
    void markPickedUp(long bookingId);

    /**
     * Admin: ACTIVE to COMPLETED. Works out the late fee, then refunds any overpayment
     * automatically, or leaves a balance for the customer to pay.
     */
    void processReturn(long bookingId, LocalDateTime returnedAt, BigDecimal damageFee);

    Optional<Invoice> findInvoice(long bookingId);

    List<Payment> listPayments(long bookingId);
}
