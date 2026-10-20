<%@ page pageEncoding="UTF-8" errorPage="/error.jsp" import="com.ngtukien.demo.dao.SponsorDAO,com.ngtukien.demo.model.*,java.sql.SQLException" %>
<%@ include file="../WEB-INF/jspf/readSponsor.jspf" %>
<%
request.setAttribute("self", true);
request.setAttribute("action", "/sponsor/do-update-profile.jsp");
Member loggedIn = (Member) session.getAttribute("member");
try (SponsorDAO dao = new SponsorDAO()) {
    dao.updateProfile(loggedIn.getId(), input);
    Sponsor updated = dao.getById(loggedIn.getId());
    updated.setUsername(loggedIn.getUsername());
    session.setAttribute("member", updated);
    session.setAttribute("sponsor", updated);
    session.setAttribute("message", "Profile updated");
    response.sendRedirect(request.getContextPath() + "/sponsor/profile.jsp");
    return;
} catch (IllegalArgumentException exception) {
    request.setAttribute("error", exception.getMessage());
} catch (SQLException exception) {
    if (!"23505".equals(exception.getSQLState())) throw exception;
    request.setAttribute("error", "Tax code already exists");
}
request.getRequestDispatcher("/WEB-INF/views/sponsor-form.jsp").forward(request, response);
%>