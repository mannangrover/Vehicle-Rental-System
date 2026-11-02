<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div class="d-flex flex-wrap align-items-center gap-2">
        <h1>Booking #${booking.id}</h1>
        <c:set var="statusValue" value="${booking.status}"/><%@ include file="/WEB-INF/jsp/common/status-badge.jspf" %>
        <c:if test="${booking.overdue}"><span class="status-badge status-overdue">overdue</span></c:if>
    </div>
    <a href="<c:url value='/bookings'/>">Back to my bookings</a>
</div>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="panel mb-3">
            <h2 class="mb-3">Rental</h2>
            <div class="summary-row">
                <span class="muted">Vehicle</span>
                <a href="<c:url value='/vehicles/${booking.vehicleId}'/>"><c:out value="${booking.vehicleName}"/></a>
            </div>
            <div class="summary-row"><span class="muted">Registration</span><span><c:out value="${booking.regNumber}"/></span></div>
            <div class="summary-row"><span class="muted">From</span><span>${booking.startLabel}</span></div>
            <div class="summary-row"><span class="muted">To</span><span>${booking.endLabel}</span></div>
            <div class="summary-row"><span class="muted">Duration</span><span>${booking.durationLabel}</span></div>
            <c:if test="${not empty booking.pickedUpAt}">
                <div class="summary-row"><span class="muted">Picked up</span><span>${booking.pickedUpLabel}</span></div>
            </c:if>
            <c:if test="${not empty booking.returnedAt}">
                <div class="summary-row"><span class="muted">Returned</span><span>${booking.returnedLabel}</span></div>
            </c:if>
        </div>

        <c:if test="${canCancel}">
            <div class="panel">
                <h2 class="mb-2">Change of plans?</h2>
                <p class="muted">You can cancel before your pick-up time. Any payments are refunded.</p>
                <form method="post" action="<c:url value='/bookings/${booking.id}/cancel'/>"
                      onsubmit="return confirm('Cancel this booking?');">
                    <button type="submit" class="btn btn-outline-danger">Cancel booking</button>
                </form>
            </div>
        </c:if>
    </div>

    <div class="col-lg-6">
        <div class="panel mb-3">
            <c:choose>
                <c:when test="${not empty invoice}">
                    <h2 class="mb-1">Invoice</h2>
                    <p class="muted small mb-3">
                        <c:out value="${invoice.invoiceNumber}"/>,
                        issued ${fn:replace(fn:substring(invoice.issuedAt.toString(),0,16),'T',' ')}
                    </p>
                    <div class="summary-row"><span class="muted">Rental</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.rentalAmount}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row"><span class="muted">Deposit held</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.depositAmount}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row"><span class="muted">Late fee</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.lateFee}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row"><span class="muted">Damage</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.damageFee}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row total"><span>Total charges</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.totalCharges}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row"><span class="muted">Paid</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.netPaid}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row total"><span>Amount due</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.amountDue}" pattern="#,##0.00"/></span></div>
                    <p class="muted small mt-2 mb-0">The deposit is held, not charged, so it isn't part of total charges. Paid is shown after refunds.</p>
                </c:when>
                <c:otherwise>
                    <h2 class="mb-3">Price</h2>
                    <div class="summary-row"><span class="muted">Rental</span><span class="money">&#8377;<fmt:formatNumber value="${booking.rentalAmount}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row"><span class="muted">Deposit</span><span class="money">&#8377;<fmt:formatNumber value="${booking.depositAmount}" pattern="#,##0.00"/></span></div>
                    <p class="muted small mt-2 mb-0">No invoice yet.</p>
                </c:otherwise>
            </c:choose>
        </div>

        <c:if test="${canPay}">
            <div class="panel">
                <h2 class="mb-3">Pay now</h2>
                <form method="post" action="<c:url value='/bookings/${booking.id}/pay'/>">
                    <div class="mb-3">
                        <label class="form-label" for="method">Payment method (simulated)</label>
                        <select class="form-select" id="method" name="method" aria-describedby="methodHelp" required>
                            <c:forEach var="m" items="${paymentMethods}">
                                <option value="${m}">${m == 'UPI' ? 'UPI' : m == 'CARD' ? 'Card' : 'Cash'}</option>
                            </c:forEach>
                        </select>
                        <div class="form-text" id="methodHelp">This is a simulation. No real money moves.</div>
                    </div>
                    <button type="submit" class="btn btn-primary">
                        Pay <span class="money">&#8377;<fmt:formatNumber value="${invoice.amountDue}" pattern="#,##0.00"/></span>
                    </button>
                </form>
            </div>
        </c:if>
    </div>

    <div class="col-12">
        <div class="panel">
            <h2 class="mb-3">Payment history</h2>
            <c:choose>
                <c:when test="${empty payments}">
                    <p class="muted mb-0">No payments yet.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                            <tr>
                                <th scope="col">Date</th>
                                <th scope="col">Type</th>
                                <th scope="col">Method</th>
                                <th scope="col">Amount</th>
                                <th scope="col">Reference</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="p" items="${payments}">
                                <tr>
                                    <td class="text-nowrap">${fn:replace(fn:substring(p.createdAt.toString(),0,16),'T',' ')}</td>
                                    <td class="text-capitalize">${fn:toLowerCase(p.type)}</td>
                                    <td>${p.method == 'UPI' ? 'UPI' : p.method == 'CARD' ? 'Card' : 'Cash'}</td>
                                    <td><span class="money">&#8377;<fmt:formatNumber value="${p.amount}" pattern="#,##0.00"/></span></td>
                                    <td><c:out value="${p.reference}"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
