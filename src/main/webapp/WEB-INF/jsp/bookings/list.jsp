<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <h1>My bookings</h1>
    <a class="btn btn-primary" href="<c:url value='/vehicles'/>">Book a vehicle</a>
</div>

<c:choose>
    <c:when test="${empty bookings}">
        <div class="panel text-center">
            <h2>No bookings yet</h2>
            <p class="muted mb-3">When you book a vehicle, it will show up here.</p>
            <a class="btn btn-primary" href="<c:url value='/vehicles'/>">Browse vehicles</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="panel">
            <div class="table-wrap">
                <table class="table table-hover">
                    <thead>
                    <tr>
                        <th scope="col">Booking</th>
                        <th scope="col">Vehicle</th>
                        <th scope="col">From</th>
                        <th scope="col">To</th>
                        <th scope="col">Duration</th>
                        <th scope="col">Rental</th>
                        <th scope="col">Status</th>
                        <th scope="col"><span class="visually-hidden">Actions</span></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="b" items="${bookings}">
                        <tr>
                            <td>#${b.id}</td>
                            <td><c:out value="${b.vehicleName}"/></td>
                            <td class="text-nowrap">${b.startLabel}</td>
                            <td class="text-nowrap">${b.endLabel}</td>
                            <td class="text-nowrap">${b.durationLabel}</td>
                            <td><span class="money">&#8377;<fmt:formatNumber value="${b.rentalAmount}" pattern="#,##0.00"/></span></td>
                            <td class="text-nowrap">
                                <c:set var="statusValue" value="${b.status}"/><%@ include file="/WEB-INF/jsp/common/status-badge.jspf" %>
                                <c:if test="${b.overdue}"><span class="status-badge status-overdue">overdue</span></c:if>
                            </td>
                            <td><a href="<c:url value='/bookings/${b.id}'/>">View</a></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
