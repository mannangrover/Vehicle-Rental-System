<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="panel text-center">
    <h1>Something went wrong</h1>
    <p class="muted">
        <c:choose>
            <c:when test="${not empty errorMessage}"><c:out value="${errorMessage}"/></c:when>
            <c:otherwise>Please try again in a moment.</c:otherwise>
        </c:choose>
    </p>
    <a class="btn btn-primary" href="<c:url value='/'/>">Go to the home page</a>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
