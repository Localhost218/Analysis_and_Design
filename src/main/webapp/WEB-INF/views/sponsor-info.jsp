<%@ page pageEncoding="UTF-8" %>
<%@ include file="../jspf/header.jspf" %>
<h1><c:out value="${sponsor.name}"/></h1>
<dl>
    <dt>Tax Code</dt><dd><c:out value="${sponsor.taxCode}"/></dd>
    <dt>Business Field</dt><dd><c:out value="${sponsor.field}"/></dd>
    <dt>Address</dt><dd><c:out value="${sponsor.address}"/></dd>
    <dt>Phone Number</dt><dd><c:out value="${sponsor.tel}"/></dd>
    <dt>Email</dt><dd><c:out value="${sponsor.email}"/></dd>
    <dt>Cooperation Status</dt><dd><c:out value="${sponsor.status.label}"/></dd>
</dl>
<c:choose>
    <c:when test="${self}"><a href="<c:url value='/sponsor/profile-form.jsp'/>">Edit Information</a></c:when>
    <c:otherwise>
        <a href="<c:url value='/organizer/sponsor-form.jsp?id=${sponsor.id}'/>">Edit Information</a>
        <form action="<c:url value='/organizer/do-delete-sponsor.jsp'/>" method="post" onsubmit="return confirm('Delete this sponsor?')">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="id" value="${sponsor.id}"><button class="danger">Delete Sponsor</button>
        </form>
    </c:otherwise>
</c:choose>
<h2>Signed Contracts</h2>
<div class="table"><table>
    <thead><tr><th>Contract Code</th><th>Signing Date</th><th>Effective From</th><th>Effective To</th><th>Status</th></tr></thead>
    <tbody>
    <c:forEach items="${contracts}" var="c">
        <tr><td><c:out value="${c.barcode}"/></td><td><c:out value="${c.signedDate}"/></td>
            <td><c:out value="${c.effectiveFrom}"/></td><td><c:out value="${c.effectiveTo}"/></td><td><c:out value="${c.status}"/></td></tr>
    </c:forEach>
    <c:if test="${empty contracts}"><tr><td colspan="5">No signed contracts.</td></tr></c:if>
    </tbody>
</table></div>
<%@ include file="../jspf/footer.jspf" %>