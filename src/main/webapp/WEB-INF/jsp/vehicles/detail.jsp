<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <h1><c:out value="${vehicle.displayName}"/></h1>
    <a href="<c:url value='/vehicles'/>">Back to vehicles</a>
</div>

<div class="vehicle-yard vehicle-showcase">
    <c:set var="artType" value="${vehicle.type}"/><%@ include file="/WEB-INF/jsp/common/vehicle-art.jspf" %>
</div>

<div class="row g-3">
    <div class="col-lg-6">
        <div class="panel h-100">
            <h2 class="mb-3">Details</h2>
            <div class="summary-row"><span class="muted">Type</span><span>${vehicle.type.label}</span></div>
            <div class="summary-row"><span class="muted">Year</span><span>${vehicle.modelYear}</span></div>
            <div class="summary-row"><span class="muted">Fuel</span><span><c:out value="${vehicle.fuelType}"/></span></div>
            <div class="summary-row"><span class="muted">Transmission</span><span><c:out value="${vehicle.transmission}"/></span></div>
            <div class="summary-row"><span class="muted">Seats</span><span>${vehicle.seats}</span></div>
            <div class="summary-row total">
                <span>Daily rate</span>
                <span class="money">&#8377;<fmt:formatNumber value="${vehicle.dailyRate}" pattern="#,##0.00"/></span>
            </div>
            <div class="summary-row fw-semibold">
                <span>Hourly rate</span>
                <span class="money">&#8377;<fmt:formatNumber value="${vehicle.hourlyRate}" pattern="#,##0.00"/></span>
            </div>
            <div class="summary-row">
                <span class="muted">Refundable deposit</span>
                <span class="money">&#8377;<fmt:formatNumber value="${vehicle.securityDeposit}" pattern="#,##0.00"/></span>
            </div>
            <div class="summary-row">
                <span class="muted">Long-rental discount</span>
                <span>${vehicle.longRentalDiscountPercent}% off 7 days or more</span>
            </div>
        </div>
    </div>

    <div class="col-lg-6">
        <div class="panel h-100">
            <h2 class="mb-3">Book this vehicle</h2>
            <c:choose>
                <c:when test="${vehicle.status != 'AVAILABLE'}">
                    <div class="alert alert-warning mb-0" role="status">This vehicle isn't available to book right now.</div>
                </c:when>
                <c:when test="${empty sessionScope.currentUser}">
                    <c:choose>
                        <c:when test="${not empty startAt and not empty endAt}">
                            <c:set var="returnPath" value="/vehicles/${vehicle.id}?startAt=${startAt}&endAt=${endAt}"/>
                        </c:when>
                        <c:otherwise>
                            <c:set var="returnPath" value="/vehicles/${vehicle.id}"/>
                        </c:otherwise>
                    </c:choose>
                    <c:url var="loginUrl" value="/login"><c:param name="next" value="${returnPath}"/></c:url>
                    <p class="muted">You need an account to book.</p>
                    <a class="btn btn-primary" href="<c:out value='${loginUrl}'/>">Log in to book</a>
                </c:when>
                <c:otherwise>
                    <form method="post" action="<c:url value='/vehicles/${vehicle.id}/book'/>" id="bookingForm"
                          data-rate="${vehicle.dailyRate}"
                          data-hourly="${vehicle.hourlyRate}"
                          data-deposit="${vehicle.securityDeposit}"
                          data-discount="${vehicle.longRentalDiscountPercent}">
                        <div class="row g-3 mb-3">
                            <div class="col-sm-6">
                                <label class="form-label" for="startAt">Pick-up time</label>
                                <input class="form-control" type="datetime-local" id="startAt" name="startAt" min="${minStart}" step="3600"
                                       value="${startAt}" required>
                            </div>
                            <div class="col-sm-6">
                                <label class="form-label" for="endAt">Return time</label>
                                <input class="form-control" type="datetime-local" id="endAt" name="endAt" min="${minStart}" step="3600"
                                       value="${endAt}" required>
                            </div>
                        </div>
                        <div id="estimate" class="mb-3" aria-live="polite" hidden>
                            <div class="summary-row"><span class="muted">Duration</span><span id="estimateDuration"></span></div>
                            <div class="summary-row"><span class="muted">Rental</span><span class="money" id="estimateRental"></span></div>
                            <div class="summary-row"><span class="muted">Refundable deposit</span><span class="money" id="estimateDeposit"></span></div>
                            <div class="summary-row total"><span>Estimated total</span><span class="money" id="estimateTotal"></span></div>
                        </div>
                        <p class="muted small" id="estimateNote">Full days use the daily rate, and extra hours never cost more than a day. Times are whole hours. The final price is calculated when you book.</p>
                        <button type="submit" class="btn btn-primary">Book now</button>
                    </form>
                    <script>
                        // Only a preview. The server works out the real price.
                        (function () {
                            var form = document.getElementById("bookingForm");
                            var start = document.getElementById("startAt");
                            var end = document.getElementById("endAt");
                            var estimate = document.getElementById("estimate");
                            var rate = parseFloat(form.dataset.rate);
                            var hourly = parseFloat(form.dataset.hourly);
                            var deposit = parseFloat(form.dataset.deposit);
                            var discountPercent = parseFloat(form.dataset.discount);
                            var millisPerHour = 60 * 60 * 1000;

                            function money(value) {
                                return "\u20B9" + value.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
                            }

                            function plural(count, word) {
                                return count + " " + word + (count === 1 ? "" : "s");
                            }

                            function durationText(days, extraHours) {
                                if (days === 0) {
                                    return plural(extraHours, "hour");
                                }
                                return plural(days, "day") + (extraHours > 0 ? " " + plural(extraHours, "hour") : "");
                            }

                            function update() {
                                if (start.value) {
                                    end.min = start.value;
                                }
                                if (!start.value || !end.value || end.value <= start.value) {
                                    estimate.hidden = true;
                                    return;
                                }
                                var hours = Math.floor((Date.parse(end.value) - Date.parse(start.value)) / millisPerHour);
                                var days = Math.floor(hours / 24);
                                var extraHours = hours % 24;
                                var rental = rate * days + Math.min(hourly * extraHours, rate);
                                if (days >= 7) {
                                    rental = rental * (100 - discountPercent) / 100;
                                }
                                document.getElementById("estimateDuration").textContent = durationText(days, extraHours);
                                document.getElementById("estimateRental").textContent = money(rental);
                                document.getElementById("estimateDeposit").textContent = money(deposit);
                                document.getElementById("estimateTotal").textContent = money(rental + deposit);
                                estimate.hidden = false;
                            }

                            start.addEventListener("change", update);
                            end.addEventListener("change", update);
                            update();
                        })();
                    </script>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
