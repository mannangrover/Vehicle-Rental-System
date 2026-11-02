<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <div>
        <h1>Booking #${booking.id}</h1>
        <c:set var="statusValue" value="${booking.status}"/><%@ include file="/WEB-INF/jsp/common/status-badge.jspf" %>
        <c:if test="${booking.overdue}"><span class="status-badge status-overdue">overdue</span></c:if>
    </div>
    <a class="btn btn-outline-secondary" href="<c:url value='/admin/bookings'/>">All bookings</a>
</div>

<div class="row g-3">
    <div class="col-lg-7 d-flex flex-column gap-3">
        <div class="panel">
            <h2>Details</h2>
            <div class="summary-row"><span class="muted">Customer</span><span class="text-end"><c:out value="${booking.customerName}"/></span></div>
            <div class="summary-row"><span class="muted">Email</span><span class="text-end text-break"><c:out value="${booking.customerEmail}"/></span></div>
            <div class="summary-row"><span class="muted">Vehicle</span><span class="text-end"><c:out value="${booking.vehicleName}"/> (${booking.vehicleType.label})</span></div>
            <div class="summary-row"><span class="muted">Registration</span><span class="text-end"><c:out value="${booking.regNumber}"/></span></div>
            <div class="summary-row"><span class="muted">From</span><span class="text-end">${booking.startLabel}</span></div>
            <div class="summary-row"><span class="muted">To</span><span class="text-end">${booking.endLabel}</span></div>
            <div class="summary-row"><span class="muted">Duration</span><span class="text-end">${booking.durationLabel}</span></div>
            <div class="summary-row"><span class="muted">Daily rate</span><span class="money">&#8377;<fmt:formatNumber value="${booking.dailyRate}" pattern="#,##0.00"/></span></div>
            <div class="summary-row"><span class="muted">Hourly rate</span><span class="money">&#8377;<fmt:formatNumber value="${booking.hourlyRate}" pattern="#,##0.00"/></span></div>
        </div>

        <div class="panel">
            <h2>Payment history</h2>
            <c:choose>
                <c:when test="${empty payments}">
                    <p class="muted mb-0">No payments yet.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-wrap">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th scope="col">Time</th>
                                    <th scope="col">Type</th>
                                    <th scope="col">Method</th>
                                    <th scope="col">Reference</th>
                                    <th scope="col" class="text-end">Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="payment" items="${payments}">
                                    <tr>
                                        <td class="text-nowrap">${fn:replace(fn:substring(payment.createdAt.toString(),0,16),'T',' ')}</td>
                                        <td class="text-capitalize">${fn:toLowerCase(payment.type)}</td>
                                        <td>${payment.method == 'UPI' ? 'UPI' : payment.method == 'CARD' ? 'Card' : 'Cash'}</td>
                                        <td><c:out value="${payment.reference}"/></td>
                                        <td class="text-end"><span class="money">&#8377;<fmt:formatNumber value="${payment.amount}" pattern="#,##0.00"/></span></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <div class="col-lg-5 d-flex flex-column gap-3">
        <div class="panel">
            <h2>Invoice</h2>
            <c:choose>
                <c:when test="${empty invoice}">
                    <p class="muted">No invoice yet.</p>
                    <div class="summary-row"><span>Rental</span><span class="money">&#8377;<fmt:formatNumber value="${booking.rentalAmount}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row"><span>Deposit (held, not charged)</span><span class="money">&#8377;<fmt:formatNumber value="${booking.depositAmount}" pattern="#,##0.00"/></span></div>
                </c:when>
                <c:otherwise>
                    <p class="muted"><c:out value="${invoice.invoiceNumber}"/>, issued ${fn:replace(fn:substring(invoice.issuedAt.toString(),0,16),'T',' ')}</p>
                    <div class="summary-row"><span>Rental</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.rentalAmount}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row"><span>Late fee</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.lateFee}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row"><span>Damage fee</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.damageFee}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row total"><span>Total charges</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.totalCharges}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row"><span>Deposit (held, not charged)</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.depositAmount}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row"><span>Paid, after refunds</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.netPaid}" pattern="#,##0.00"/></span></div>
                    <div class="summary-row total"><span>Amount due</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.amountDue}" pattern="#,##0.00"/></span></div>
                </c:otherwise>
            </c:choose>
        </div>

        <c:if test="${booking.status == 'CONFIRMED'}">
            <div class="panel">
                <h2>Pickup</h2>
                <c:if test="${awaitingPayment}">
                    <p class="muted">Waiting for payment from the customer.</p>
                </c:if>
                <c:if test="${startsLater}">
                    <p class="muted">Pickup opens ${earlyPickupHours} hour before the start time, ${booking.startLabel}.</p>
                </c:if>
                <form method="post" action="<c:url value='/admin/bookings/${booking.id}/pickup'/>" class="m-0">
                    <button type="submit" class="btn btn-primary" ${canPickUp ? '' : 'disabled'}>Mark as picked up</button>
                </form>
            </div>
        </c:if>

        <c:if test="${booking.status == 'ACTIVE'}">
            <div class="panel">
                <h2>Return</h2>
                <form method="post" action="<c:url value='/admin/bookings/${booking.id}/return'/>" class="m-0">
                    <div class="mb-3">
                        <label class="form-label" for="returnedAt">Returned at</label>
                        <input class="form-control" type="datetime-local" id="returnedAt" name="returnedAt" value="${now}"
                               min="${booking.pickedUpAt}" max="${now}" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label" for="damageFee">Damage fee (&#8377;)</label>
                        <input class="form-control" type="number" id="damageFee" name="damageFee" value="0" min="0" step="0.01">
                    </div>
                    <p class="muted small">
                        There is no late fee up to ${booking.endLabel}. After that, each hour or part of an hour costs
                        <span class="money">&#8377;<fmt:formatNumber value="${lateFeePerHour}" pattern="#,##0.00"/></span>,
                        up to <span class="money">&#8377;<fmt:formatNumber value="${lateFeePerDay}" pattern="#,##0.00"/></span>
                        a day (the normal price times this vehicle type's late multiplier). Any overpayment is refunded
                        automatically, and any balance is left for the customer to pay.
                    </p>
                    <button type="submit" class="btn btn-primary">Record return</button>
                </form>
            </div>
        </c:if>

        <c:if test="${booking.status == 'COMPLETED'}">
            <div class="panel">
                <h2>Settlement</h2>
                <div class="summary-row"><span>Picked up</span><span>${booking.pickedUpLabel}</span></div>
                <div class="summary-row"><span>Returned</span><span>${booking.returnedLabel}</span></div>
                <c:choose>
                    <c:when test="${not empty invoice and invoice.amountDue > 0}">
                        <div class="summary-row total"><span>Balance due from customer</span><span class="money">&#8377;<fmt:formatNumber value="${invoice.amountDue}" pattern="#,##0.00"/></span></div>
                    </c:when>
                    <c:otherwise>
                        <p class="muted mt-2 mb-0">Settled. The customer owes nothing.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
