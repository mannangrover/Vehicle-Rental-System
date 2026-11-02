package com.vehiclerental.service.impl;

import com.vehiclerental.dao.UserDao;
import com.vehiclerental.dto.RegisterForm;
import com.vehiclerental.exception.AuthenticationFailedException;
import com.vehiclerental.exception.DuplicateAccountException;
import com.vehiclerental.exception.RentalException;
import com.vehiclerental.model.Role;
import com.vehiclerental.model.SessionUser;
import com.vehiclerental.model.User;
import com.vehiclerental.service.AuthService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int MAX_PASSWORD_BYTES = 72;

    private final UserDao userDao;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserDao userDao, BCryptPasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public SessionUser register(RegisterForm form) {
        // BCrypt's limit is in bytes, and the form's 72-character check lets longer non-ASCII passwords through.
        if (form.getPassword().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new RentalException("That password is too long. Please choose a shorter one.");
        }
        User user = new User();
        user.setFullName(form.getFullName().trim());
        user.setEmail(normalizeEmail(form.getEmail()));
        user.setPhone(form.getPhone().trim());
        user.setLicenseNo(form.getLicenseNo().trim());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.CUSTOMER);
        try {
            long id = userDao.insert(user);
            return new SessionUser(id, user.getFullName(), user.getEmail(), user.getRole());
        } catch (DuplicateKeyException e) {
            throw new DuplicateAccountException("An account with that email already exists");
        }
    }

    @Override
    public SessionUser login(String email, String password) {
        if (email == null || password == null) {
            throw new AuthenticationFailedException("Incorrect email or password");
        }
        // The same message for both failures, so the form can't reveal which emails are registered.
        User user = userDao.findByEmail(normalizeEmail(email))
                .filter(found -> passwordEncoder.matches(password, found.getPasswordHash()))
                .orElseThrow(() -> new AuthenticationFailedException("Incorrect email or password"));
        return new SessionUser(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
