<%@ page pageEncoding="UTF-8" errorPage="/error.jsp" import="com.ngtukien.demo.dao.*" %>
<%
try {
    int id = Integer.parseInt(request.getParameter("id"));
    try (SponsorDAO dao = new SponsorDAO(); ContractDAO contracts = new ContractDAO()) {
        request.setAttribute("sponsor", dao.getById(id));
        request.setAttribute("contracts", contracts.getSignedBySponsor(id));
    }
} catch (IllegalArgumentException exception) { response.sendError(404); return; }
request.setAttribute("self", false);
%>
<jsp:include page="/WEB-INF/views/sponsor-info.jsp"/>