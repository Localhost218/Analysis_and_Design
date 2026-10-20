<%@ page pageEncoding="UTF-8" %>
<%@ include file="../WEB-INF/jspf/header.jspf" %>
<h1>Organizer</h1>
<dl>
    <dt>Full Name</dt><dd><c:out value="${sessionScope.organizer.fullName}"/></dd>
    <dt>Address</dt><dd><c:out value="${sessionScope.organizer.address}"/></dd>
    <dt>Date of Birth</dt><dd><c:out value="${sessionScope.organizer.dateOfBirth}"/></dd>
    <dt>Email</dt><dd><c:out value="${sessionScope.organizer.email}"/></dd>
</dl>
<h2><a href="<c:url value='/organizer/sponsor-management.jsp'/>">Sponsor Management</a></h2>
<h2>Sponsorship Contract Signing</h2><p>Module 2 — not implemented yet.</p>
<h2>Race Stage Reward Payment</h2><p>Module 3 — not implemented yet.</p>
<%@ include file="../WEB-INF/jspf/footer.jspf" %>