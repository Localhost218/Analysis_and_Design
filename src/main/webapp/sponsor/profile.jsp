<%@ page pageEncoding="UTF-8" errorPage="/error.jsp" import="com.ngtukien.demo.dao.*,com.ngtukien.demo.model.Member" %>
<%
int id = ((Member) session.getAttribute("member")).getId();
try (SponsorDAO dao = new SponsorDAO(); ContractDAO contracts = new ContractDAO()) {
    request.setAttribute("sponsor", dao.getById(id));
    request.setAttribute("contracts", contracts.getSignedBySponsor(id));
}
request.setAttribute("self", true);
%>
<jsp:include page="/WEB-INF/views/sponsor-info.jsp"/>