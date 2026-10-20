<%@ page pageEncoding="UTF-8" errorPage="/error.jsp" import="com.ngtukien.demo.dao.SponsorDAO,com.ngtukien.demo.model.Sponsor" %>
<%
Sponsor sponsor = new Sponsor();
String id = request.getParameter("id");
if (id != null && !id.isBlank()) {
    try (SponsorDAO dao = new SponsorDAO()) { sponsor = dao.getById(Integer.parseInt(id)); }
    catch (IllegalArgumentException exception) { response.sendError(404); return; }
}
request.setAttribute("sponsorForm", sponsor);
request.setAttribute("self", false);
request.setAttribute("action", "/organizer/do-save-sponsor.jsp");
%>
<jsp:include page="/WEB-INF/views/sponsor-form.jsp"/>