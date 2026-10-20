<%@ page pageEncoding="UTF-8" errorPage="/error.jsp" import="com.ngtukien.demo.dao.SponsorDAO,java.sql.SQLException" %>
<%
try (SponsorDAO dao = new SponsorDAO()) {
    dao.delete(Integer.parseInt(request.getParameter("id")));
    session.setAttribute("message", "Sponsor deleted");
} catch (IllegalArgumentException exception) {
    session.setAttribute("message", exception.getMessage());
} catch (SQLException exception) {
    if (!"23503".equals(exception.getSQLState())) throw exception;
    session.setAttribute("message", "Cannot delete a sponsor that is currently referenced");
}
response.sendRedirect(request.getContextPath() + "/organizer/sponsor-management.jsp");
%>