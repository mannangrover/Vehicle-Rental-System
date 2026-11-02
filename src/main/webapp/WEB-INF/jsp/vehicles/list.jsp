<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <h1>Find a vehicle</h1>
</div>

<div class="panel mb-4">
<form method="get" action="<c:url value='/vehicles'/>" class="row g-3 align-items-end">
    <div class="col-sm-6 col-lg-3">
        <label class="form-label" for="startAt">Pick-up time</label>
        <input class="form-control" type="datetime-local" id="startAt" name="startAt" min="${minStart}" step="3600"
               value="<c:out value='${criteria.startAt}'/>">
    </div>
    <div class="col-sm-6 col-lg-3">
        <label class="form-label" for="endAt">Return time</label>
        <input class="form-control" type="datetime-local" id="endAt" name="endAt" min="${minStart}" step="3600"
               value="<c:out value='${criteria.endAt}'/>">
    </div>
    <div class="col-sm-6 col-lg-2">
        <label class="form-label" for="type">Type</label>
        <select class="form-select" id="type" name="type">
            <option value="">Any type</option>
            <c:forEach var="t" items="${vehicleTypes}">
                <option value="${t}" ${criteria.type == t ? 'selected' : ''}>${t.label}</option>
            </c:forEach>
        </select>
    </div>
    <div class="col-sm-6 col-lg-2">
        <label class="form-label" for="maxDailyRate">Max per day (&#8377;)</label>
        <input class="form-control" type="number" id="maxDailyRate" name="maxDailyRate" min="0" step="any"
               value="<c:out value='${criteria.maxDailyRate}'/>">
    </div>
    <div class="col-lg-2 d-flex gap-2">
        <button type="submit" class="btn btn-primary flex-fill">Search</button>
        <a class="btn btn-outline-secondary" href="<c:url value='/vehicles'/>">Clear</a>
    </div>
    <div class="col-12 mt-2">
        <p class="muted small mb-0">Enter both times to see only vehicles free for your whole trip. Rentals run in whole hours.</p>
    </div>
</form>
</div>

<c:choose>
    <c:when test="${empty vehicles}">
        <div class="panel text-center">
            <h2>No vehicles match your search</h2>
            <p class="muted mb-3">Try different times, another type or a higher daily limit.</p>
            <a class="btn btn-outline-secondary" href="<c:url value='/vehicles'/>">See all vehicles</a>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row g-3">
            <c:forEach var="v" items="${vehicles}">
                <%@ include file="/WEB-INF/jsp/vehicles/card.jspf" %>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
