<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <h1>Dashboard</h1>
    <div class="d-flex flex-wrap gap-2">
        <a class="btn btn-primary" href="<c:url value='/admin/bookings'/>">Manage bookings</a>
        <a class="btn btn-outline-secondary" href="<c:url value='/admin/reports/bookings.csv'/>">Download bookings CSV</a>
    </div>
</div>

<div class="stat-strip">
    <div class="stat stat-revenue">
        <div class="stat-value"><span class="money">&#8377;<fmt:formatNumber value="${stats.totalRevenue}" pattern="#,##0"/></span></div>
        <div class="stat-label">Revenue earned</div>
    </div>
    <div class="stat">
        <div class="stat-value">${stats.fleetUsePercent}%</div>
        <div class="stat-label">Fleet in use</div>
    </div>
    <div class="stat">
        <div class="stat-value">${stats.vehiclesOut}<span class="fs-5 fw-semibold muted"> of ${stats.totalVehicles}</span></div>
        <div class="stat-label">Vehicles out</div>
    </div>
    <div class="stat">
        <div class="stat-value">${stats.upcomingBookings}</div>
        <div class="stat-label">Upcoming bookings</div>
    </div>
    <div class="stat ${stats.overdueRentals > 0 ? 'stat-alert' : ''}">
        <div class="stat-value">${stats.overdueRentals}</div>
        <div class="stat-label">Overdue rentals</div>
    </div>
    <div class="stat">
        <div class="stat-value">${stats.vehiclesInMaintenance}</div>
        <div class="stat-label">In maintenance</div>
    </div>
</div>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="panel h-100">
            <h2>Bookings by status</h2>
            <c:set var="bookingTotal" value="${0}"/>
            <c:forEach var="entry" items="${stats.bookingsByStatus}">
                <c:set var="bookingTotal" value="${bookingTotal + entry.value}"/>
            </c:forEach>
            <c:choose>
                <c:when test="${bookingTotal > 0}">
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                                <tr><th scope="col">Status</th><th scope="col" class="text-end">Bookings</th><th scope="col" class="w-50">Share</th></tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${stats.bookingsByStatus}">
                                    <tr>
                                        <td><c:set var="statusValue" value="${entry.key}"/><%@ include file="/WEB-INF/jsp/common/status-badge.jspf" %></td>
                                        <td class="text-end">${entry.value}</td>
                                        <td>
                                            <div class="progress" aria-hidden="true">
                                                <div class="progress-bar" style="width: ${entry.value * 100 / bookingTotal}%; background-color: var(--app-accent);"></div>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="muted mb-0">No bookings yet.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="col-lg-6">
        <div class="panel h-100">
            <h2>Revenue by vehicle type</h2>
            <c:set var="revenueTotal" value="${0}"/>
            <c:forEach var="entry" items="${stats.revenueByType}">
                <c:set var="revenueTotal" value="${revenueTotal + entry.value}"/>
            </c:forEach>
            <%-- Test with > because EL's == on BigDecimal uses equals(), where 0.00 is not 0 --%>
            <c:choose>
                <c:when test="${revenueTotal > 0}">
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                                <tr><th scope="col">Type</th><th scope="col" class="text-end">Revenue</th><th scope="col" class="w-50">Share</th></tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${stats.revenueByType}">
                                    <tr>
                                        <td>${entry.key == 'SUV' ? 'SUV' : entry.key == 'CAR' ? 'Car' : 'Bike'}</td>
                                        <td class="text-end"><span class="money">&#8377;<fmt:formatNumber value="${entry.value}" pattern="#,##0.00"/></span></td>
                                        <td>
                                            <div class="progress" aria-hidden="true">
                                                <div class="progress-bar" style="width: ${entry.value * 100 / revenueTotal}%; background-color: var(--app-accent);"></div>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="muted mb-0">No revenue yet.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
