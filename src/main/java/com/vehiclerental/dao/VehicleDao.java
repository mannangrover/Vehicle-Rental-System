package com.vehiclerental.dao;

import com.vehiclerental.model.Vehicle;
import com.vehiclerental.model.VehicleFactory;
import com.vehiclerental.model.VehicleStatus;
import com.vehiclerental.model.VehicleType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class VehicleDao {

    private static final RowMapper<Vehicle> VEHICLE_MAPPER = (rs, rowNum) -> {
        Vehicle vehicle = VehicleFactory.create(VehicleType.valueOf(rs.getString("type")));
        vehicle.setId(rs.getLong("id"));
        vehicle.setBrand(rs.getString("brand"));
        vehicle.setModel(rs.getString("model"));
        vehicle.setModelYear(rs.getInt("model_year"));
        vehicle.setRegNumber(rs.getString("reg_number"));
        vehicle.setFuelType(rs.getString("fuel_type"));
        vehicle.setTransmission(rs.getString("transmission"));
        vehicle.setSeats(rs.getInt("seats"));
        vehicle.setDailyRate(rs.getBigDecimal("daily_rate"));
        vehicle.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        vehicle.setStatus(VehicleStatus.valueOf(rs.getString("status")));
        return vehicle;
    };

    private final JdbcTemplate jdbcTemplate;

    public VehicleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insert(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles (type, brand, model, model_year, reg_number, fuel_type, "
                + "transmission, seats, daily_rate, hourly_rate, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, vehicle.getType().name());
            ps.setString(2, vehicle.getBrand());
            ps.setString(3, vehicle.getModel());
            ps.setInt(4, vehicle.getModelYear());
            ps.setString(5, vehicle.getRegNumber());
            ps.setString(6, vehicle.getFuelType());
            ps.setString(7, vehicle.getTransmission());
            ps.setInt(8, vehicle.getSeats());
            ps.setBigDecimal(9, vehicle.getDailyRate());
            ps.setBigDecimal(10, vehicle.getHourlyRate());
            ps.setString(11, vehicle.getStatus().name());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    /** Status is left alone: it changes only through updateStatus. */
    public void update(Vehicle vehicle) {
        jdbcTemplate.update("UPDATE vehicles SET type = ?, brand = ?, model = ?, model_year = ?, reg_number = ?, "
                        + "fuel_type = ?, transmission = ?, seats = ?, daily_rate = ?, hourly_rate = ? WHERE id = ?",
                vehicle.getType().name(), vehicle.getBrand(), vehicle.getModel(), vehicle.getModelYear(),
                vehicle.getRegNumber(), vehicle.getFuelType(), vehicle.getTransmission(), vehicle.getSeats(),
                vehicle.getDailyRate(), vehicle.getHourlyRate(), vehicle.getId());
    }

    public void updateStatus(long id, VehicleStatus status) {
        jdbcTemplate.update("UPDATE vehicles SET status = ? WHERE id = ?", status.name(), id);
    }

    /** Holds the row lock until the transaction ends. Returns false if there is no such vehicle. */
    public boolean lockById(long id) {
        return !jdbcTemplate.queryForList("SELECT id FROM vehicles WHERE id = ? FOR UPDATE", Long.class, id).isEmpty();
    }

    public Optional<Vehicle> findById(long id) {
        return jdbcTemplate.query("SELECT * FROM vehicles WHERE id = ?", VEHICLE_MAPPER, id)
                .stream().findFirst();
    }

    public List<Vehicle> findAll() {
        return jdbcTemplate.query("SELECT * FROM vehicles ORDER BY id", VEHICLE_MAPPER);
    }

    public List<Vehicle> findAvailable() {
        return jdbcTemplate.query("SELECT * FROM vehicles WHERE status = 'AVAILABLE' ORDER BY id", VEHICLE_MAPPER);
    }

    /** AVAILABLE vehicles with no conflicting booking for the given times. */
    public List<Vehicle> findAvailableBetween(LocalDateTime startAt, LocalDateTime endAt, LocalDateTime now) {
        String sql = "SELECT * FROM vehicles v WHERE v.status = 'AVAILABLE' AND NOT EXISTS ("
                + "SELECT 1 FROM bookings b WHERE b.vehicle_id = v.id AND " + BookingDao.CONFLICT_CONDITION
                + ") ORDER BY v.id";
        return jdbcTemplate.query(sql, VEHICLE_MAPPER, endAt, startAt, startAt, now);
    }
}
