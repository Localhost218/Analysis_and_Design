<%@ page pageEncoding="UTF-8" isErrorPage="true" %>
<%
response.setStatus(500);
if (exception != null) application.log("Unable to process the request", exception);
%>
<%@ include file="WEB-INF/jspf/header.jspf" %>
<h1>Unable to process the request</h1>
<p>Please check your input or try again later.</p>
<a href="<c:url value='/login.jsp'/>">Back to Sign In</a>
<%@ include file="WEB-INF/jspf/footer.jspf" %>