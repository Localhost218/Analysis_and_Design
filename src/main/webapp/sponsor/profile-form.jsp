<%@ page pageEncoding="UTF-8" errorPage="/error.jsp" import="com.ngtukien.demo.dao.SponsorDAO,com.ngtukien.demo.model.Member" %>
<%
try (SponsorDAO dao = new SponsorDAO()) {
    request.setAttribute("sponsorForm", dao.getById(((Member) session.getAttribute("member")).getId()));
}
request.setAttribute("self", true);
request.setAttribute("action", "/sponsor/do-update-profile.jsp");
%>
<jsp:include page="/WEB-INF/views/sponsor-form.jsp"/>