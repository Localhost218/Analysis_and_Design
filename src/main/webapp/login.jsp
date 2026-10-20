<%@ page pageEncoding="UTF-8" %>
<%@ include file="WEB-INF/jspf/header.jspf" %>
<h1>Sign In</h1>
<c:if test="${param.err == 'fail'}"><p class="error">Invalid username or password</p></c:if>
<c:if test="${param.err == 'timeout'}"><p>Your session has expired. Please sign in again.</p></c:if>
<form action="<c:url value='/do-login.jsp'/>" method="post">
    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
    <label>Username<input name="username" required autocomplete="username"></label>
    <label>Password<input name="password" type="password" required autocomplete="current-password"></label>
    <button>Sign In</button>
</form>
<%@ include file="WEB-INF/jspf/footer.jspf" %>