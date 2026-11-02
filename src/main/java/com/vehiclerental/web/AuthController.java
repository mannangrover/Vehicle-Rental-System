package com.vehiclerental.web;

import com.vehiclerental.config.DemoAccounts;
import com.vehiclerental.dto.RegisterForm;
import com.vehiclerental.exception.AuthenticationFailedException;
import com.vehiclerental.exception.DuplicateAccountException;
import com.vehiclerental.model.SessionUser;
import com.vehiclerental.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;
    private final boolean demoDataEnabled;

    public AuthController(AuthService authService, @Value("${app.seed-demo-data:false}") boolean demoDataEnabled) {
        this.authService = authService;
        this.demoDataEnabled = demoDataEnabled;
    }

    /** Fills the login page's demo buttons. The accounts only exist when demo data is on. */
    @ModelAttribute
    public void addDemoAccounts(Model model) {
        if (demoDataEnabled) {
            model.addAttribute("demoCustomer", DemoAccounts.CUSTOMER);
            model.addAttribute("demoAdmin", DemoAccounts.ADMIN);
        }
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String next, HttpServletRequest request, Model model) {
        SessionUser user = currentUser(request);
        if (user != null) {
            return "redirect:" + landingPage(next, user);
        }
        model.addAttribute("pageTitle", "Log in");
        model.addAttribute("next", isSafeLocalPath(next) ? next : null);
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(defaultValue = "") String email,
                        @RequestParam(defaultValue = "") String password,
                        @RequestParam(required = false) String next,
                        HttpServletRequest request,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        try {
            SessionUser user = authService.login(email, password);
            startSession(request, user);
            redirectAttributes.addFlashAttribute("success", "Welcome back, " + user.getFullName() + ".");
            return "redirect:" + landingPage(next, user);
        } catch (AuthenticationFailedException e) {
            model.addAttribute("pageTitle", "Log in");
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("next", isSafeLocalPath(next) ? next : null);
            return "auth/login";
        }
    }

    @GetMapping("/register")
    public String registerForm(@RequestParam(required = false) String next, HttpServletRequest request, Model model) {
        SessionUser user = currentUser(request);
        if (user != null) {
            return "redirect:" + landingPage(next, user);
        }
        model.addAttribute("pageTitle", "Sign up");
        model.addAttribute("form", new RegisterForm());
        model.addAttribute("next", isSafeLocalPath(next) ? next : null);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegisterForm form,
                           BindingResult bindingResult,
                           @RequestParam(required = false) String next,
                           HttpServletRequest request,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                SessionUser user = authService.register(form);
                startSession(request, user);
                redirectAttributes.addFlashAttribute("success", "Your account is ready. Welcome, " + user.getFullName() + ".");
                return "redirect:" + landingPage(next, user);
            } catch (DuplicateAccountException e) {
                model.addAttribute("error", e.getMessage());
            }
        }
        model.addAttribute("errors", bindingResult.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .toList());
        model.addAttribute("pageTitle", "Sign up");
        model.addAttribute("next", isSafeLocalPath(next) ? next : null);
        form.setPassword(null);
        return "auth/register";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirectAttributes.addFlashAttribute("success", "You have been logged out.");
        return "redirect:/";
    }

    /**
     * Changing the session id at login means an id an attacker planted before
     * login is useless afterwards (session fixation).
     */
    private void startSession(HttpServletRequest request, SessionUser user) {
        HttpSession session = request.getSession();
        request.changeSessionId();
        session.setAttribute(SessionUser.SESSION_KEY, user);
    }

    private SessionUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
    }

    private String landingPage(String next, SessionUser user) {
        if (isSafeLocalPath(next)) {
            return next;
        }
        return user.isAdmin() ? "/admin" : "/";
    }

    /**
     * True only for a path on this site. Browsers treat "//host" and "/\host" as another
     * website and drop tabs and newlines from URLs, so all of those are refused. "{" is
     * refused because Spring would read it as a URI template variable in the redirect.
     */
    static boolean isSafeLocalPath(String path) {
        if (path == null || !path.startsWith("/") || path.startsWith("//")) {
            return false;
        }
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c <= ' ' || c == '\\' || c == '{' || c == '}') {
                return false;
            }
        }
        return true;
    }
}
