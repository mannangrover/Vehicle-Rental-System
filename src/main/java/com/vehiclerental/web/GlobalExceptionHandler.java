package com.vehiclerental.web;

import com.vehiclerental.exception.AccessDeniedException;
import com.vehiclerental.exception.RentalException;
import com.vehiclerental.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.net.URISyntaxException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(ResourceNotFoundException e, Model model) {
        model.addAttribute("pageTitle", "Not found");
        model.addAttribute("errorMessage", e.getMessage());
        return "errors/not-found";
    }

    // Spring raises these for a URL that matches nothing. Without this handler they would
    // reach the catch-all below and show as a 500.
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noSuchPage(Model model) {
        model.addAttribute("pageTitle", "Not found");
        return "errors/not-found";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String forbidden(AccessDeniedException e, Model model) {
        model.addAttribute("pageTitle", "Not allowed");
        model.addAttribute("errorMessage", e.getMessage());
        return "errors/forbidden";
    }

    @ExceptionHandler(RentalException.class)
    public String rentalError(RentalException e,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if ("POST".equals(request.getMethod())) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:" + refererPath(request);
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        model.addAttribute("pageTitle", "Something went wrong");
        model.addAttribute("errorMessage", e.getMessage());
        return "errors/error";
    }

    @ExceptionHandler({MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String badRequest(Model model) {
        model.addAttribute("pageTitle", "Bad request");
        model.addAttribute("errorMessage", "A value in that link or form is missing or could not be read.");
        return "errors/error";
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public String methodNotAllowed(Model model) {
        model.addAttribute("pageTitle", "Something went wrong");
        model.addAttribute("errorMessage", "That page can't be opened this way.");
        return "errors/error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String unexpected(Exception e, HttpServletRequest request, Model model) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), e);
        model.addAttribute("pageTitle", "Something went wrong");
        model.addAttribute("errorMessage", "Something went wrong on our side. Please try again.");
        return "errors/error";
    }

    /**
     * Where to send the user back to after a failed POST. Only the path and query of the
     * Referer are used, and only if it came from this site, so this can never redirect
     * to another host.
     */
    private String refererPath(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null) {
            return "/";
        }
        URI uri;
        try {
            uri = new URI(referer);
        } catch (URISyntaxException e) {
            return "/";
        }
        if (!request.getServerName().equalsIgnoreCase(uri.getHost())) {
            return "/";
        }

        // "redirect:" adds the context path back on, so take it off here
        String path = uri.getRawPath();
        String contextPath = request.getContextPath();
        if (path != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (!AuthController.isSafeLocalPath(path)) {
            return "/";
        }
        return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
    }
}
