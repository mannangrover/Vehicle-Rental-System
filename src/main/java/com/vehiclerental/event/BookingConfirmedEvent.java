package com.vehiclerental.event;

/** Published when a customer's booking is confirmed. */
public class BookingConfirmedEvent {

    private final long bookingId;

    public BookingConfirmedEvent(long bookingId) {
        this.bookingId = bookingId;
    }

    public long getBookingId() {
        return bookingId;
    }
}
