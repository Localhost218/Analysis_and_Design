<%@ page pageEncoding="UTF-8" errorPage="/error.jsp" import="com.ngtukien.demo.dao.SponsorDAO" %>
<%
String keyword = request.getParameter("keyword");
if (keyword == null) keyword = "";
request.setAttribute("keyword", keyword);
try (SponsorDAO dao = new SponsorDAO()) { request.setAttribute("sponsors", dao.searchByName(keyword)); }
%>
<%@ include file="../WEB-INF/jspf/header.jspf" %>
<h1>Sponsor Management</h1>
<form method="get" action="<c:url value='/organizer/sponsor-management.jsp'/>">
    <label>Sponsor Name<input name="keyword" value="<c:out value='${keyword}'/>" maxlength="200"></label>
    <button>Search</button> <a href="<c:url value='/organizer/sponsor-form.jsp'/>">Add Sponsor</a>
</form>
<div class="table"><table>
    <thead><tr><th>Sponsor Name</th><th>Tax Code</th><th>Address</th><th>Phone Number</th><th>Email</th><th></th></tr></thead>
    <tbody>
    <c:forEach items="${sponsors}" var="s">
        <tr>
            <td><c:out value="${s.name}"/></td><td><c:out value="${s.taxCode}"/></td>
            <td><c:out value="${s.address}"/></td><td><c:out value="${s.tel}"/></td><td><c:out value="${s.email}"/></td>
            <td><a href="<c:url value='/organizer/sponsor-info.jsp?id=${s.id}'/>">Details</a></td>
        </tr>
    </c:forEach>
    <c:if test="${empty sponsors}"><tr><td colspan="6">No sponsors found.</td></tr></c:if>
    </tbody>
</table></div>
<%@ include file="../WEB-INF/jspf/footer.jspf" %>