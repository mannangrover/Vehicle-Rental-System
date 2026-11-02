package com.vehiclerental.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Repository
public class DashboardDao {

    private final JdbcTemplate jdbcTemplate;

    public DashboardDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public BigDecimal totalRevenue() {
        return jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(rental_amount + late_fee + damage_fee), 0) FROM bookings WHERE status = 'COMPLETED'",
                BigDecimal.class);
    }

    public int countNonRetiredVehicles() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vehicles WHERE status <> 'RETIRED'", Integer.class);
    }

    public int countVehiclesInMaintenance() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vehicles WHERE status = 'MAINTENANCE'", Integer.class);
    }

    public int countOverdueRentals() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE status = 'ACTIVE' AND overdue = TRUE", Integer.class);
    }

    /** Only statuses that have bookings appear in the result. */
    public Map<String, Integer> countBookingsByStatus() {
        Map<String, Integer> counts = new HashMap<>();
        jdbcTemplate.query("SELECT status, COUNT(*) AS total FROM bookings GROUP BY status",
                rs -> {
                    counts.put(rs.getString("status"), rs.getInt("total"));
                });
        return counts;
    }

    /** Only vehicle types that have earned revenue appear in the result. */
    public Map<String, BigDecimal> revenueByVehicleType() {
        Map<String, BigDecimal> revenue = new HashMap<>();
        jdbcTemplate.query("SELECT v.type AS vehicle_type, SUM(b.rental_amount + b.late_fee + b.damage_fee) AS total "
                        + "FROM bookings b JOIN vehicles v ON v.id = b.vehicle_id "
                        + "WHERE b.status = 'COMPLETED' GROUP BY v.type",
                rs -> {
                    revenue.put(rs.getString("vehicle_type"), rs.getBigDecimal("total"));
                });
        return revenue;
    }
}
