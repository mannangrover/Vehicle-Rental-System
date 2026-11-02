<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<section class="hero">
    <div class="container">
        <div class="hero-top">
            <div>
                <h1>Self-drive cars, bikes and SUVs, by the hour or the day.</h1>
                <p class="hero-lead">Book from one hour to 30 days and see the full price first. Rent for a week or more and the rate drops.</p>
            </div>
            <div class="hero-plate" aria-hidden="true">
                <span class="plate plate-lg"><span class="plate-ind">IND</span><span class="plate-text">RR 01 READY</span></span>
            </div>
        </div>
        <form method="get" action="<c:url value='/vehicles'/>" class="hero-search row g-3 align-items-end mx-0">
            <div class="col-sm-6 col-lg-3">
                <label class="form-label" for="startAt">Pick-up time</label>
                <input class="form-control" type="datetime-local" id="startAt" name="startAt" min="${minStart}" step="3600">
            </div>
            <div class="col-sm-6 col-lg-3">
                <label class="form-label" for="endAt">Return time</label>
                <input class="form-control" type="datetime-local" id="endAt" name="endAt" min="${minStart}" step="3600">
            </div>
            <div class="col-sm-6 col-lg-3">
                <label class="form-label" for="type">Vehicle type</label>
                <select class="form-select" id="type" name="type">
                    <option value="">Any type</option>
                    <c:forEach var="t" items="${vehicleTypes}">
                        <option value="${t}">${t.label}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-sm-6 col-lg-3">
                <button type="submit" class="btn btn-primary w-100">Search vehicles</button>
            </div>
        </form>
    </div>
    <div class="hero-road" aria-hidden="true"></div>
</section>

<c:if test="${not empty featured}">
    <section class="mb-5">
        <div class="page-head">
            <h2>Available now</h2>
            <a href="<c:url value='/vehicles'/>">See all vehicles</a>
        </div>
        <div class="row g-3">
            <c:forEach var="v" items="${featured}">
                <%@ include file="/WEB-INF/jsp/vehicles/card.jspf" %>
            </c:forEach>
        </div>
    </section>
</c:if>

<section class="mb-4">
    <h2 class="mb-3">How renting works</h2>
    <ol class="steps">
        <li class="step">
            <span class="step-number" aria-hidden="true">1</span>
            <h3>Book your times</h3>
            <p>Need it for a few hours or a few days? Find a vehicle that's free and book it straight away.</p>
        </li>
        <li class="step">
            <span class="step-number" aria-hidden="true">2</span>
            <h3>Pay</h3>
            <p>Your booking is confirmed at once. Pay the rental and a refundable deposit.</p>
        </li>
        <li class="step">
            <span class="step-number" aria-hidden="true">3</span>
            <h3>Drive and return</h3>
            <p>Collect the vehicle at your start time. Your deposit comes back after the return.</p>
        </li>
    </ol>
</section>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
