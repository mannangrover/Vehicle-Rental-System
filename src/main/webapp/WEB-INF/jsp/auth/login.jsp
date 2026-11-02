<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="auth-layout">
    <div class="auth-form">
        <h1 class="mb-3">Log in</h1>
        <form method="post" action="<c:url value='/login'/>">
            <c:if test="${not empty next}">
                <input type="hidden" name="next" value="<c:out value='${next}'/>">
            </c:if>
            <div class="mb-3">
                <label class="form-label" for="email">Email</label>
                <input class="form-control" type="email" id="email" name="email" autocomplete="username" required
                       value="<c:out value='${email}'/>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="password">Password</label>
                <input class="form-control" type="password" id="password" name="password"
                       autocomplete="current-password" required>
            </div>
            <button type="submit" class="btn btn-primary w-100">Log in</button>
        </form>
        <c:url var="registerUrl" value="/register">
            <c:if test="${not empty next}"><c:param name="next" value="${next}"/></c:if>
        </c:url>
        <p class="mt-3 mb-0">New here? <a href="<c:out value='${registerUrl}'/>">Create an account</a></p>
    </div>

    <c:if test="${not empty demoCustomer}">
        <aside class="demo-panel" aria-labelledby="demoHeading">
            <h2 id="demoHeading">Try a demo account</h2>
            <p>Log in with one click to explore the app with sample data.</p>
            <form method="post" action="<c:url value='/login'/>">
                <input type="hidden" name="email" value="<c:out value='${demoCustomer.email}'/>">
                <input type="hidden" name="password" value="<c:out value='${demoCustomer.password}'/>">
                <c:if test="${not empty next}"><input type="hidden" name="next" value="<c:out value='${next}'/>"></c:if>
                <button type="submit" class="demo-login">
                    <strong>Log in as customer</strong>
                    <span>Search vehicles, book, pay and see invoices</span>
                </button>
            </form>
            <form method="post" action="<c:url value='/login'/>">
                <input type="hidden" name="email" value="<c:out value='${demoAdmin.email}'/>">
                <input type="hidden" name="password" value="<c:out value='${demoAdmin.password}'/>">
                <button type="submit" class="demo-login">
                    <strong>Log in as admin</strong>
                    <span>Approve bookings, manage the fleet and see the dashboard</span>
                </button>
            </form>
        </aside>
    </c:if>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
