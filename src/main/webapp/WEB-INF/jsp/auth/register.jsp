<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="form-narrow">
    <div class="panel">
        <h1 class="mb-3">Create your account</h1>
        <form method="post" action="<c:url value='/register'/>">
            <c:if test="${not empty next}">
                <input type="hidden" name="next" value="<c:out value='${next}'/>">
            </c:if>
            <div class="mb-3">
                <label class="form-label" for="fullName">Full name</label>
                <input class="form-control" type="text" id="fullName" name="fullName" maxlength="100"
                       autocomplete="name" required value="<c:out value='${form.fullName}'/>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="email">Email</label>
                <input class="form-control" type="email" id="email" name="email" maxlength="150"
                       autocomplete="email" required value="<c:out value='${form.email}'/>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="phone">Phone</label>
                <input class="form-control" type="tel" id="phone" name="phone" maxlength="20"
                       autocomplete="tel" required value="<c:out value='${form.phone}'/>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="licenseNo">Driving licence number</label>
                <input class="form-control" type="text" id="licenseNo" name="licenseNo" maxlength="30"
                       required value="<c:out value='${form.licenseNo}'/>">
            </div>
            <div class="mb-3">
                <label class="form-label" for="password">Password</label>
                <input class="form-control" type="password" id="password" name="password" minlength="8" maxlength="72"
                       autocomplete="new-password" aria-describedby="passwordHelp" required>
                <div class="form-text" id="passwordHelp">At least 8 characters.</div>
            </div>
            <button type="submit" class="btn btn-primary w-100">Sign up</button>
        </form>
        <c:url var="loginUrl" value="/login">
            <c:if test="${not empty next}"><c:param name="next" value="${next}"/></c:if>
        </c:url>
        <p class="mt-3 mb-0">Already have an account? <a href="<c:out value='${loginUrl}'/>">Log in</a></p>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
