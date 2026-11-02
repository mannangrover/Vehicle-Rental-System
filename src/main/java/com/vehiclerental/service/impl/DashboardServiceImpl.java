package com.vehiclerental.service.impl;

import com.vehiclerental.dao.AuditLogDao;
import com.vehiclerental.dao.DashboardDao;
import com.vehiclerental.model.AuditLog;
import com.vehiclerental.model.BookingStatus;
import com.vehiclerental.model.DashboardStats;
import com.vehiclerental.model.VehicleType;
import com.vehiclerental.service.DashboardService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final DashboardDao dashboardDao;
    private final AuditLogDao auditLogDao;
    private final ExecutorService executor;

    public DashboardServiceImpl(DashboardDao dashboardDao, AuditLogDao auditLogDao,
                                @Qualifier("dashboardExecutor") ExecutorService executor) {
        this.dashboardDao = dashboardDao;
        this.auditLogDao = auditLogDao;
        this.executor = executor;
    }

    @Override
    public DashboardStats getStats() {
        Callable<BigDecimal> revenueTask = dashboardDao::totalRevenue;
        Callable<Integer> vehiclesTask = dashboardDao::countNonRetiredVehicles;
        Callable<Integer> maintenanceTask = dashboardDao::countVehiclesInMaintenance;
        Callable<Integer> overdueTask = dashboardDao::countOverdueRentals;
        Callable<Map<String, Integer>> statusTask = dashboardDao::countBookingsByStatus;
        Callable<Map<String, BigDecimal>> typeTask = dashboardDao::revenueByVehicleType;

        Future<BigDecimal> revenue = executor.submit(revenueTask);
        Future<Integer> vehicles = executor.submit(vehiclesTask);
        Future<Integer> maintenance = executor.submit(maintenanceTask);
        Future<Integer> overdue = executor.submit(overdueTask);
        Future<Map<String, Integer>> byStatus = executor.submit(statusTask);
        Future<Map<String, BigDecimal>> byType = executor.submit(typeTask);

        Map<String, Integer> statusCounts = await(byStatus);
        Map<String, Integer> bookingsByStatus = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) {
            bookingsByStatus.put(status.name(), statusCounts.getOrDefault(status.name(), 0));
        }

        Map<String, BigDecimal> typeRevenue = await(byType);
        Map<String, BigDecimal> revenueByType = new LinkedHashMap<>();
        for (VehicleType type : VehicleType.values()) {
            revenueByType.put(type.name(), typeRevenue.getOrDefault(type.name(), BigDecimal.ZERO));
        }

        int totalVehicles = await(vehicles);
        int vehiclesOut = bookingsByStatus.get(BookingStatus.ACTIVE.name());

        DashboardStats stats = new DashboardStats();
        stats.setTotalRevenue(await(revenue));
        stats.setTotalVehicles(totalVehicles);
        stats.setVehiclesOut(vehiclesOut);
        stats.setVehiclesInMaintenance(await(maintenance));
        stats.setUpcomingBookings(bookingsByStatus.get(BookingStatus.CONFIRMED.name()));
        stats.setOverdueRentals(await(overdue));
        stats.setFleetUsePercent(totalVehicles == 0 ? 0 : vehiclesOut * 100 / totalVehicles);
        stats.setBookingsByStatus(bookingsByStatus);
        stats.setRevenueByType(revenueByType);
        return stats;
    }

    @Override
    public List<AuditLog> recentAuditLogs(int limit) {
        return auditLogDao.findRecent(limit);
    }

    private <T> T await(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            // Restore the flag so code further up can still see this thread was interrupted.
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while loading dashboard statistics", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Could not load dashboard statistics", e.getCause());
        }
    }
}
