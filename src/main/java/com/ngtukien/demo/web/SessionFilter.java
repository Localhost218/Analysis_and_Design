package com.ngtukien.demo.web;

import com.ngtukien.demo.model.Member;
import com.ngtukien.demo.model.Organizer;
import com.ngtukien.demo.model.Sponsor;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class SessionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getServletPath();
        var session = req.getSession();
        if (session.getAttribute("csrf") == null) session.setAttribute("csrf", UUID.randomUUID().toString());
        if (path.startsWith("/assets/")) { chain.doFilter(request, response); return; }
        res.setHeader("Cache-Control", "no-store");
        if (path.contains("/do-") && !req.getMethod().equals("POST")) {
            res.sendError(405); return;
        }
        if (req.getMethod().equals("POST") && !session.getAttribute("csrf").equals(req.getParameter("csrf"))) {
            res.sendError(403); return;
        }
        boolean publicPage = path.equals("/") || path.equals("/index.jsp") || path.equals("/login.jsp")
                || path.equals("/do-login.jsp") || path.equals("/error.jsp");
        Member member = (Member) session.getAttribute("member");
        if (!publicPage && member == null) {
            res.sendRedirect(req.getContextPath() + "/login.jsp?err=timeout"); return;
        }
        if ((path.startsWith("/organizer/") && !(member instanceof Organizer))
                || (path.startsWith("/sponsor/") && !(member instanceof Sponsor))) {
            res.sendError(403); return;
        }
        chain.doFilter(request, response);
    }
}
