package com.vehiclerental.dao;

import com.vehiclerental.model.Payment;
import com.vehiclerental.model.PaymentMethod;
import com.vehiclerental.model.PaymentType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentDao {

    private static final RowMapper<Payment> PAYMENT_MAPPER = (rs, rowNum) -> {
        Payment payment = new Payment();
        payment.setId(rs.getLong("id"));
        payment.setBookingId(rs.getLong("booking_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setMethod(PaymentMethod.valueOf(rs.getString("method")));
        payment.setType(PaymentType.valueOf(rs.getString("type")));
        payment.setReference(rs.getString("reference"));
        payment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return payment;
    };

    private final JdbcTemplate jdbcTemplate;

    public PaymentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insert(long bookingId, BigDecimal amount, PaymentMethod method, PaymentType type, String reference) {
        String sql = "INSERT INTO payments (booking_id, amount, method, type, reference) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"});
            ps.setLong(1, bookingId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, method.name());
            ps.setString(4, type.name());
            ps.setString(5, reference);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Optional<Payment> findById(long id) {
        return jdbcTemplate.query("SELECT * FROM payments WHERE id = ?", PAYMENT_MAPPER, id)
                .stream().findFirst();
    }

    public List<Payment> findByBooking(long bookingId) {
        return jdbcTemplate.query("SELECT * FROM payments WHERE booking_id = ? ORDER BY id", PAYMENT_MAPPER, bookingId);
    }

    /** Payments minus refunds. */
    public BigDecimal netPaid(long bookingId) {
        return jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(CASE WHEN type = 'PAYMENT' THEN amount ELSE -amount END), 0) "
                        + "FROM payments WHERE booking_id = ?", BigDecimal.class, bookingId);
    }

    /** The method of the most recent PAYMENT, used to send a refund back the same way. */
    public Optional<PaymentMethod> findLatestPaymentMethod(long bookingId) {
        return jdbcTemplate.queryForList(
                        "SELECT method FROM payments WHERE booking_id = ? AND type = 'PAYMENT' ORDER BY id DESC LIMIT 1",
                        String.class, bookingId)
                .stream().findFirst().map(PaymentMethod::valueOf);
    }
}
