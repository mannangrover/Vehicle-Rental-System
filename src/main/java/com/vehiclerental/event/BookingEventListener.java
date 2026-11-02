package com.vehiclerental.event;

import com.vehiclerental.dao.InvoiceDao;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class BookingEventListener {

    private final InvoiceDao invoiceDao;
    private final Clock clock;

    public BookingEventListener(InvoiceDao invoiceDao, Clock clock) {
        this.invoiceDao = invoiceDao;
        this.clock = clock;
    }

    /** Runs synchronously inside the booking transaction, so booking and invoice succeed or fail together. */
    @EventListener
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        long bookingId = event.getBookingId();
        int year = LocalDate.now(clock).getYear();
        invoiceDao.insert(bookingId, InvoiceDao.invoiceNumber(year, bookingId));
    }
}
