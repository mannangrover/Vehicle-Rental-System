package com.vehiclerental.model;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardStats {

    private BigDecimal totalRevenue;
    private int totalVehicles;
    private int vehiclesOut;
    private int vehiclesInMaintenance;
    private int upcomingBookings;
    private int overdueRentals;
    private int fleetUsePercent;
    /** Keyed by status name so JSP can read it, e.g. ${stats.bookingsByStatus['ACTIVE']}. */
    private Map<String, Integer> bookingsByStatus;
    /** Earned revenue by vehicle type name. */
    private Map<String, BigDecimal> revenueByType;

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public int getTotalVehicles() { return totalVehicles; }
    public void setTotalVehicles(int totalVehicles) { this.totalVehicles = totalVehicles; }
    public int getVehiclesOut() { return vehiclesOut; }
    public void setVehiclesOut(int vehiclesOut) { this.vehiclesOut = vehiclesOut; }
    public int getVehiclesInMaintenance() { return vehiclesInMaintenance; }
    public void setVehiclesInMaintenance(int vehiclesInMaintenance) { this.vehiclesInMaintenance = vehiclesInMaintenance; }
    public int getUpcomingBookings() { return upcomingBookings; }
    public void setUpcomingBookings(int upcomingBookings) { this.upcomingBookings = upcomingBookings; }
    public int getOverdueRentals() { return overdueRentals; }
    public void setOverdueRentals(int overdueRentals) { this.overdueRentals = overdueRentals; }
    public int getFleetUsePercent() { return fleetUsePercent; }
    public void setFleetUsePercent(int fleetUsePercent) { this.fleetUsePercent = fleetUsePercent; }
    public Map<String, Integer> getBookingsByStatus() { return bookingsByStatus; }
    public void setBookingsByStatus(Map<String, Integer> bookingsByStatus) { this.bookingsByStatus = bookingsByStatus; }
    public Map<String, BigDecimal> getRevenueByType() { return revenueByType; }
    public void setRevenueByType(Map<String, BigDecimal> revenueByType) { this.revenueByType = revenueByType; }
}
