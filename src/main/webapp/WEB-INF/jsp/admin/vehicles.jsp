<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <h1>Fleet</h1>
    <a class="btn btn-primary" href="<c:url value='/admin/vehicles/new'/>">Add vehicle</a>
</div>

<div class="panel">
    <c:choose>
        <c:when test="${empty vehicles}">
            <p class="muted mb-0">No vehicles yet.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table class="table">
                    <thead>
                        <tr>
                            <th scope="col">Type</th>
                            <th scope="col">Name</th>
                            <th scope="col">Reg number</th>
                            <th scope="col" class="text-end">Daily rate</th>
                            <th scope="col" class="text-end">Hourly rate</th>
                            <th scope="col">Status</th>
                            <th scope="col">Change status</th>
                            <th scope="col"><span class="visually-hidden">Edit</span></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="v" items="${vehicles}">
                            <tr>
                                <td>${v.type.label}</td>
                                <td><c:out value="${v.displayName}"/> <span class="muted">${v.modelYear}</span></td>
                                <td class="text-nowrap"><c:out value="${v.regNumber}"/></td>
                                <td class="text-end"><span class="money">&#8377;<fmt:formatNumber value="${v.dailyRate}" pattern="#,##0.00"/></span></td>
                                <td class="text-end"><span class="money">&#8377;<fmt:formatNumber value="${v.hourlyRate}" pattern="#,##0.00"/></span></td>
                                <td><c:set var="statusValue" value="${v.status}"/><%@ include file="/WEB-INF/jsp/common/status-badge.jspf" %></td>
                                <td>
                                    <form method="post" action="<c:url value='/admin/vehicles/${v.id}/status'/>" class="d-flex gap-2 m-0">
                                        <label class="visually-hidden" for="status-${v.id}">Status for vehicle ${v.id}</label>
                                        <select class="form-select form-select-sm w-auto" id="status-${v.id}" name="status">
                                            <c:forEach var="s" items="${vehicleStatuses}">
                                                <option value="${s}" ${v.status == s ? 'selected' : ''}>${s.label}</option>
                                            </c:forEach>
                                        </select>
                                        <button type="submit" class="btn btn-sm btn-outline-secondary">Set</button>
                                    </form>
                                </td>
                                <td><a href="<c:url value='/admin/vehicles/${v.id}/edit'/>">Edit<span class="visually-hidden"> vehicle ${v.id}</span></a></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
