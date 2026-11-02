<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <h1>Manage bookings</h1>
    <a class="btn btn-outline-secondary" href="<c:url value='/admin/reports/bookings.csv'/>">Download CSV</a>
</div>

<nav class="d-flex flex-wrap gap-2 mb-3" aria-label="Filter by status">
    <a class="btn btn-sm ${empty selectedStatus ? 'btn-primary' : 'btn-outline-secondary'}"
       aria-current="${empty selectedStatus ? 'true' : 'false'}"
       href="<c:url value='/admin/bookings'/>">All</a>
    <c:forEach var="s" items="${statuses}">
        <a class="btn btn-sm ${selectedStatus == s ? 'btn-primary' : 'btn-outline-secondary'}"
           aria-current="${selectedStatus == s ? 'true' : 'false'}"
           href="<c:url value='/admin/bookings?status=${s}'/>">${s.label}</a>
    </c:forEach>
</nav>

<div class="panel">
    <c:choose>
        <c:when test="${empty bookings}">
            <p class="muted mb-0">
                <c:choose>
                    <c:when test="${empty selectedStatus}">No bookings yet.</c:when>
                    <c:otherwise>No ${fn:toLowerCase(selectedStatus)} bookings.</c:otherwise>
                </c:choose>
            </p>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table class="table">
                    <thead>
                        <tr>
                            <th scope="col">Customer</th>
                            <th scope="col">Vehicle</th>
                            <th scope="col">Rental period</th>
                            <th scope="col" class="text-end">Amount</th>
                            <th scope="col">Status</th>
                            <th scope="col"><span class="visually-hidden">Details</span></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="b" items="${bookings}">
                            <tr>
                                <td>
                                    <c:out value="${b.customerName}"/>
                                    <div class="muted small"><c:out value="${b.customerEmail}"/></div>
                                </td>
                                <td>
                                    <c:out value="${b.vehicleName}"/>
                                    <div class="muted small"><c:out value="${b.regNumber}"/></div>
                                </td>
                                <td class="text-nowrap">${b.startLabel} to ${b.endLabel}<div class="small muted">${b.durationLabel}</div></td>
                                <td class="text-end"><span class="money">&#8377;<fmt:formatNumber value="${b.rentalAmount + b.lateFee + b.damageFee}" pattern="#,##0.00"/></span></td>
                                <td class="text-nowrap">
                                    <c:set var="statusValue" value="${b.status}"/><%@ include file="/WEB-INF/jsp/common/status-badge.jspf" %>
                                    <c:if test="${b.overdue}"><span class="status-badge status-overdue">overdue</span></c:if>
                                </td>
                                <td><a href="<c:url value='/admin/bookings/${b.id}'/>">View<span class="visually-hidden"> booking ${b.id}</span></a></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
