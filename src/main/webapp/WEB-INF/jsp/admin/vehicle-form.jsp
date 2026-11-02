<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <h1>${editing ? 'Edit vehicle' : 'Add vehicle'}</h1>
    <a class="btn btn-outline-secondary" href="<c:url value='/admin/vehicles'/>">Back to fleet</a>
</div>

<c:choose>
    <c:when test="${editing}"><c:url var="formAction" value="/admin/vehicles/${vehicleId}"/></c:when>
    <c:otherwise><c:url var="formAction" value="/admin/vehicles"/></c:otherwise>
</c:choose>

<c:set var="fuelTypes" value="Petrol,Diesel,CNG,Electric,Hybrid"/>
<c:set var="transmissions" value="Manual,Automatic"/>

<div class="panel">
    <form method="post" action="${formAction}" class="m-0">
        <div class="row g-3">
            <div class="col-md-6">
                <label class="form-label" for="type">Type</label>
                <select class="form-select" id="type" name="type" required>
                    <option value="">Choose a type</option>
                    <c:forEach var="option" items="CAR,BIKE,SUV">
                        <option value="${option}" ${form.type == option ? 'selected' : ''}>${option == 'CAR' ? 'Car' : option == 'BIKE' ? 'Bike' : 'SUV'}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="regNumber">Registration number</label>
                <input class="form-control" type="text" id="regNumber" name="regNumber" maxlength="20" required
                       value="<c:out value='${form.regNumber}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label" for="brand">Brand</label>
                <input class="form-control" type="text" id="brand" name="brand" maxlength="50" required
                       value="<c:out value='${form.brand}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label" for="model">Model</label>
                <input class="form-control" type="text" id="model" name="model" maxlength="50" required
                       value="<c:out value='${form.model}'/>">
            </div>
            <div class="col-md-6">
                <label class="form-label" for="fuelType">Fuel type</label>
                <select class="form-select" id="fuelType" name="fuelType" required>
                    <option value="">Choose a fuel type</option>
                    <c:forEach var="option" items="${fuelTypes}">
                        <option value="${option}" ${form.fuelType == option ? 'selected' : ''}>${option}</option>
                    </c:forEach>
                    <%-- Keep an older value that isn't in the list, so saving doesn't silently change it --%>
                    <c:if test="${not empty form.fuelType and not fn:contains(fuelTypes, form.fuelType)}">
                        <option value="<c:out value='${form.fuelType}'/>" selected><c:out value="${form.fuelType}"/></option>
                    </c:if>
                </select>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="transmission">Transmission</label>
                <select class="form-select" id="transmission" name="transmission" required>
                    <option value="">Choose a transmission</option>
                    <c:forEach var="option" items="${transmissions}">
                        <option value="${option}" ${form.transmission == option ? 'selected' : ''}>${option}</option>
                    </c:forEach>
                    <c:if test="${not empty form.transmission and not fn:contains(transmissions, form.transmission)}">
                        <option value="<c:out value='${form.transmission}'/>" selected><c:out value="${form.transmission}"/></option>
                    </c:if>
                </select>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="modelYear">Model year</label>
                <input class="form-control" type="number" id="modelYear" name="modelYear" min="2000" max="2100" required
                       value="<c:out value='${form.modelYear}'/>">
            </div>
            <div class="col-md-4">
                <label class="form-label" for="seats">Seats</label>
                <input class="form-control" type="number" id="seats" name="seats" min="1" max="12" required
                       value="<c:out value='${form.seats}'/>">
            </div>
            <div class="col-md-4">
                <label class="form-label" for="dailyRate">Daily rate (&#8377;)</label>
                <input class="form-control" type="number" id="dailyRate" name="dailyRate" min="1" step="0.01" required
                       value="<c:out value='${form.dailyRate}'/>">
            </div>
            <div class="col-md-4">
                <label class="form-label" for="hourlyRate">Hourly rate (&#8377;)</label>
                <input class="form-control" type="number" id="hourlyRate" name="hourlyRate" min="1" step="0.01" required
                       value="<c:out value='${form.hourlyRate}'/>">
            </div>
        </div>
        <button type="submit" class="btn btn-primary mt-3">${editing ? 'Save changes' : 'Add vehicle'}</button>
    </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
