package com.vehiclerental.service;

import com.vehiclerental.model.AuditLog;
import com.vehiclerental.model.DashboardStats;

import java.util.List;

public interface DashboardService {

    DashboardStats getStats();

    List<AuditLog> recentAuditLogs(int limit);
}
