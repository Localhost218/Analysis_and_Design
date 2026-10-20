<%@ page pageEncoding="UTF-8" errorPage="/error.jsp" import="com.ngtukien.demo.dao.SponsorDAO,com.ngtukien.demo.model.Sponsor,com.ngtukien.demo.model.enums.CooperationStatus,java.sql.SQLException" %>
<%@ include file="../WEB-INF/jspf/readSponsor.jspf" %>
<%
request.setAttribute("self", false);
request.setAttribute("action", "/organizer/do-save-sponsor.jsp");
try {
    String id = request.getParameter("id");
    if (id != null && !id.isBlank()) input.setId(Integer.valueOf(id));
    input.setStatus(CooperationStatus.valueOf(request.getParameter("status") == null ? "ACTIVE" : request.getParameter("status")));
    try (SponsorDAO dao = new SponsorDAO()) {
        if (input.getId() == null) dao.create(input); else dao.update(input.getId(), input);
    }
    session.setAttribute("message", "Sponsor information saved");
    response.sendRedirect(request.getContextPath() + "/organizer/sponsor-info.jsp?id=" + input.getId());
    return;
} catch (IllegalArgumentException exception) {
    request.setAttribute("error", exception.getMessage());
} catch (SQLException exception) {
    if (!"23505".equals(exception.getSQLState())) throw exception;
    request.setAttribute("error", "Tax code already exists");
}
request.getRequestDispatcher("/WEB-INF/views/sponsor-form.jsp").forward(request, response);
%>