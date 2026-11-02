package com.vehiclerental.web.filter;

import com.vehiclerental.model.SessionUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Login and role checks for every request. /admin needs an admin; /bookings and
 * booking a vehicle need a logged-in user; everything else is public.
 */
public class AuthFilter extends OncePerRequestFilter {

    private static final Pattern VEHICLE_BOOK_PATH = Pattern.compile("(/vehicles/[^/]+)/book");
    private static final Pattern BOOKING_ACTION_PATH = Pattern.compile("(/bookings/[^/]+)/.+");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = pathOf(request);
        boolean post = "POST".equals(request.getMethod());
        SessionUser user = currentUser(request);

        if (path.equals("/admin") || path.startsWith("/admin/")) {
            if (user == null) {
                redirectToLogin(request, response, post ? "/admin" : path);
            } else if (!user.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            } else {
                chain.doFilter(request, response);
            }
            return;
        }

        Matcher vehicleBook = VEHICLE_BOOK_PATH.matcher(path);
        boolean bookingPage = path.equals("/bookings") || path.startsWith("/bookings/");
        if (user == null && post && vehicleBook.matches()) {
            redirectToLogin(request, response, vehicleBook.group(1));
            return;
        }
        if (user == null && bookingPage) {
            redirectToLogin(request, response, post ? pageBehindAction(path) : path);
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Uses the servlet path, not the raw request URI. The container has already removed
     * ";params", decoded the path and collapsed "//" and "..", so a URL such as
     * /admin;x=1 can't slip past the checks while still reaching the admin controller.
     */
    private String pathOf(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        return request.getServletPath() + (pathInfo == null ? "" : pathInfo);
    }

    private SessionUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
    }

    /** /bookings/5/pay only accepts POST, so after logging in the user goes back to /bookings/5 instead. */
    private String pageBehindAction(String path) {
        Matcher action = BOOKING_ACTION_PATH.matcher(path);
        return action.matches() ? action.group(1) : path;
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response, String next)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/login?next="
                + URLEncoder.encode(next, StandardCharsets.UTF_8));
    }
}
