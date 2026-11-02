package com.vehiclerental.dao;

import com.vehiclerental.model.Booking;
import com.vehiclerental.model.BookingStatus;
import com.vehiclerental.model.VehicleType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingDao {

    /**
     * True for a booking (alias b) that blocks new times. A rental may start at the moment
     * another ends, and an overdue vehicle still out blocks rentals starting up to now.
     * Parameters in order: newEnd, newStart, newStart, now.
     * The CASTs are there because H2 cannot work out the type when both sides are parameters.
     */
    static final String CONFLICT_CONDITION =
            "b.status IN ('CONFIRMED', 'ACTIVE') AND b.start_at < ? "
            + "AND (b.end_at > ? OR (b.status = 'ACTIVE' AND CAST(? AS DATETIME) <= CAST(? AS DATETIME)))";

    private static final String SELECT_BOOKING =
            "SELECT b.*, v.brand, v.model, v.reg_number, v.type AS vehicle_type, "
            + "u.full_name AS customer_name, u.email AS customer_email "
            + "FROM bookings b "
            + "JOIN vehicles v ON v.id = b.vehicle_id "
            + "JOIN users u ON u.id = b.user_id ";

    private static final RowMapper<Booking> BOOKING_MAPPER = (rs, rowNum) -> {
        Booking booking = new Booking();
        booking.setId(rs.getLong("id"));
        booking.setUserId(rs.getLong("user_id"));
        booking.setVehicleId(rs.getLong("vehicle_id"));
        booking.setStartAt(rs.getObject("start_at", LocalDateTime.class));
        booking.setEndAt(rs.getObject("end_at", LocalDateTime.class));
        booking.setDailyRate(rs.getBigDecimal("daily_rate"));
        booking.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        booking.setRentalAmount(rs.getBigDecimal("rental_amount"));
        booking.setDepositAmount(rs.getBigDecimal("deposit_amount"));
        booking.setLateFee(rs.getBigDecimal("late_fee"));
        booking.setDamageFee(rs.getBigDecimal("damage_fee"));
        booking.setStatus(BookingStatus.valueOf(rs.getString("status")));
        booking.setOverdue(rs.getBoolean("overdue"));
        booking.setPickedUpAt(rs.getObject("picked_up_at", LocalDateTime.class));
        booking.setReturnedAt(rs.getObject("returned_at", LocalDateTime.class));
        booking.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        booking.setVehicleName(rs.getString("brand") + " " + rs.getString("model"));
        booking.setRegNumber(rs.getString("reg_number"));
        booking.setVehicleType(VehicleType.valueOf(rs.getString("vehicle_type")));
        booking.setCustomerName(rs.getString("customer_name"));
        booking.setCustomerEmail(rs.getString("customer_email"));
        return booking;
    };

    private final JdbcTemplate jdbcTemplate;

    public BookingDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insert(Booking booking) {
        String sql = "INSERT INTO bookings (user_id, vehicle_id, start_at, end_at, daily_rate, hourly_rate, rental_amount, "
                + "deposit_amount, late_fee, damage_fee, status, overdue, picked_up_at, returned_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"});
            ps.setLong(1, booking.getUserId());
            ps.setLong(2, booking.getVehicleId());
            ps.setObject(3, booking.getStartAt());
            ps.setObject(4, booking.getEndAt());
            ps.setBigDecimal(5, booking.getDailyRate());
            ps.setBigDecimal(6, booking.getHourlyRate());
            ps.setBigDecimal(7, booking.getRentalAmount());
            ps.setBigDecimal(8, booking.getDepositAmount());
            ps.setBigDecimal(9, booking.getLateFee());
            ps.setBigDecimal(10, booking.getDamageFee());
            ps.setString(11, booking.getStatus().name());
            ps.setBoolean(12, booking.isOverdue());
            ps.setObject(13, booking.getPickedUpAt());
            ps.setObject(14, booking.getReturnedAt());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    /** Holds the row lock until the transaction ends. Returns false if there is no such booking. */
    public boolean lockById(long id) {
        return !jdbcTemplate.queryForList("SELECT id FROM bookings WHERE id = ? FOR UPDATE", Long.class, id).isEmpty();
    }

    public Optional<Booking> findById(long id) {
        return jdbcTemplate.query(SELECT_BOOKING + "WHERE b.id = ?", BOOKING_MAPPER, id)
                .stream().findFirst();
    }

    public List<Booking> findByUser(long userId) {
        return jdbcTemplate.query(SELECT_BOOKING + "WHERE b.user_id = ? ORDER BY b.id DESC", BOOKING_MAPPER, userId);
    }

    public List<Booking> findAll() {
        return jdbcTemplate.query(SELECT_BOOKING + "ORDER BY b.id DESC", BOOKING_MAPPER);
    }

    public List<Booking> findByStatus(BookingStatus status) {
        return jdbcTemplate.query(SELECT_BOOKING + "WHERE b.status = ? ORDER BY b.id DESC", BOOKING_MAPPER, status.name());
    }

    public int countConflicts(long vehicleId, LocalDateTime startAt, LocalDateTime endAt, LocalDateTime now) {
        String sql = "SELECT COUNT(*) FROM bookings b WHERE b.vehicle_id = ? AND " + CONFLICT_CONDITION;
        return jdbcTemplate.queryForObject(sql, Integer.class, vehicleId, endAt, startAt, startAt, now);
    }

    public int countActiveForVehicle(long vehicleId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE vehicle_id = ? AND status = 'ACTIVE'", Integer.class, vehicleId);
    }

    public void updateStatus(long id, BookingStatus status) {
        jdbcTemplate.update("UPDATE bookings SET status = ? WHERE id = ?", status.name(), id);
    }

    public void markPickedUp(long id, LocalDateTime pickedUpAt) {
        jdbcTemplate.update("UPDATE bookings SET status = 'ACTIVE', picked_up_at = ? WHERE id = ?", pickedUpAt, id);
    }

    public void markReturned(long id, LocalDateTime returnedAt, BigDecimal lateFee, BigDecimal damageFee) {
        jdbcTemplate.update("UPDATE bookings SET status = 'COMPLETED', overdue = FALSE, returned_at = ?, "
                + "late_fee = ?, damage_fee = ? WHERE id = ?", returnedAt, lateFee, damageFee, id);
    }

    /** Returns how many bookings were newly marked. */
    public int markOverdue(LocalDateTime now) {
        return jdbcTemplate.update("UPDATE bookings SET overdue = TRUE "
                + "WHERE status = 'ACTIVE' AND end_at < ? AND overdue = FALSE", now);
    }
}
