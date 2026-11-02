package com.vehiclerental.dao;

import com.vehiclerental.model.Invoice;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class InvoiceDao {

    private static final RowMapper<Invoice> INVOICE_MAPPER = (rs, rowNum) -> {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(rs.getString("invoice_number"));
        invoice.setBookingId(rs.getLong("booking_id"));
        invoice.setIssuedAt(rs.getTimestamp("issued_at").toLocalDateTime());
        return invoice;
    };

    private final JdbcTemplate jdbcTemplate;

    public InvoiceDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static String invoiceNumber(int year, long bookingId) {
        return String.format("INV-%d-%05d", year, bookingId);
    }

    public void insert(long bookingId, String invoiceNumber) {
        jdbcTemplate.update("INSERT INTO invoices (booking_id, invoice_number) VALUES (?, ?)", bookingId, invoiceNumber);
    }

    /** The amounts on the returned invoice are not set: BookingService works them out. */
    public Optional<Invoice> findByBookingId(long bookingId) {
        return jdbcTemplate.query("SELECT * FROM invoices WHERE booking_id = ?", INVOICE_MAPPER, bookingId)
                .stream().findFirst();
    }
}
