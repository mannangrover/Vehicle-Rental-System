<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="page-head">
    <h1>Audit log</h1>
    <span class="muted">Most recent 100 entries</span>
</div>

<div class="panel">
    <c:choose>
        <c:when test="${empty logs}">
            <p class="muted mb-0">Nothing has been logged yet.</p>
        </c:when>
        <c:otherwise>
            <div class="table-wrap">
                <table class="table">
                    <thead>
                        <tr>
                            <th scope="col">Time</th>
                            <th scope="col">Action</th>
                            <th scope="col">Details</th>
                            <th scope="col">Actor</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="log" items="${logs}">
                            <tr>
                                <td class="text-nowrap"><c:out value="${fn:replace(fn:substring(log.createdAt.toString(),0,16),'T',' ')}"/></td>
                                <td><c:out value="${log.action}"/></td>
                                <td><c:out value="${log.details}"/></td>
                                <td><c:out value="${log.actor}"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
