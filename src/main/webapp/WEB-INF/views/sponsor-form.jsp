<%@ page pageEncoding="UTF-8" import="com.ngtukien.demo.model.enums.CooperationStatus" %>
<%@ include file="../jspf/header.jspf" %>
<h1>${self ? 'Edit Sponsor Profile' : (empty sponsorForm.id ? 'Add Sponsor' : 'Edit Sponsor')}</h1>
<form action="<c:url value='${action}'/>" method="post">
    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
    <c:if test="${not self}"><input type="hidden" name="id" value="${sponsorForm.id}"></c:if>
    <label>Sponsor Name<input name="name" value="<c:out value='${sponsorForm.name}'/>" required maxlength="200"></label>
    <label>Tax Code<input name="taxCode" value="<c:out value='${sponsorForm.taxCode}'/>" required maxlength="14"></label>
    <label>Business Field<input name="field" value="<c:out value='${sponsorForm.field}'/>" required maxlength="200"></label>
    <label>Address<input name="address" value="<c:out value='${sponsorForm.address}'/>" required maxlength="255"></label>
    <label>Phone Number<input name="tel" value="<c:out value='${sponsorForm.tel}'/>" required maxlength="20"></label>
    <label>Email<input name="email" type="email" value="<c:out value='${sponsorForm.email}'/>" required maxlength="200"></label>
    <c:if test="${not self}">
        <label>Cooperation Status<select name="status">
            <c:forEach items="<%= CooperationStatus.values() %>" var="status">
                <option value="${status.name()}" <c:if test="${status == sponsorForm.status}">selected</c:if>><c:out value="${status.label}"/></option>
            </c:forEach>
        </select></label>
    </c:if>
    <button>Save</button>
    <c:choose>
        <c:when test="${self}"><a href="<c:url value='/sponsor/profile.jsp'/>">Cancel</a></c:when>
        <c:otherwise><a href="<c:url value='/organizer/sponsor-management.jsp'/>">Cancel</a></c:otherwise>
    </c:choose>
</form>
<%@ include file="../jspf/footer.jspf" %>