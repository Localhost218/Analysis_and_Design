<%@ page pageEncoding="UTF-8" errorPage="/error.jsp" import="com.ngtukien.demo.dao.MemberDAO,com.ngtukien.demo.model.*" %>
<%
String username = request.getParameter("username");
String password = request.getParameter("password");
Member member = null;
if (username != null && password != null) {
    try (MemberDAO dao = new MemberDAO()) { member = dao.checkLogin(username, password); }
}
if (member == null) {
    response.sendRedirect(request.getContextPath() + "/login.jsp?err=fail");
    return;
}
request.changeSessionId();
session.setAttribute("member", member);
session.removeAttribute("organizer");
session.removeAttribute("sponsor");
if (member instanceof Organizer) {
    session.setAttribute("organizer", member);
    response.sendRedirect(request.getContextPath() + "/organizer/organizer-home.jsp");
} else {
    session.setAttribute("sponsor", member);
    response.sendRedirect(request.getContextPath() + "/sponsor/profile.jsp");
}
%>