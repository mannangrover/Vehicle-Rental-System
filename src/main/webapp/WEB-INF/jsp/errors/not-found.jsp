<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="panel text-center">
    <h1>Page not found</h1>
    <p class="muted">
        <c:choose>
            <c:when test="${not empty errorMessage}"><c:out value="${errorMessage}"/></c:when>
            <c:otherwise>We couldn't find what you were looking for.</c:otherwise>
        </c:choose>
    </p>
    <a class="btn btn-primary" href="<c:url value='/'/>">Go to the home page</a>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
