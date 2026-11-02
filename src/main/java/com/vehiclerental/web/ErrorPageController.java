package com.vehiclerental.web;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/** Renders errors that never reached a controller, such as sendError(403) from AuthFilter. */
@Controller
public class ErrorPageController implements ErrorController {

    @RequestMapping("/error")
    public String error(HttpServletRequest request, HttpServletResponse response, Model model) {
        Object code = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = code instanceof Integer ? (Integer) code : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        response.setStatus(status);

        if (status == HttpServletResponse.SC_NOT_FOUND) {
            model.addAttribute("pageTitle", "Not found");
            return "errors/not-found";
        }
        if (status == HttpServletResponse.SC_FORBIDDEN) {
            model.addAttribute("pageTitle", "Not allowed");
            return "errors/forbidden";
        }
        model.addAttribute("pageTitle", "Something went wrong");
        return "errors/error";
    }
}
