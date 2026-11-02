package com.vehiclerental.service;

import com.vehiclerental.dto.RegisterForm;
import com.vehiclerental.model.SessionUser;

public interface AuthService {

    /** @throws com.vehiclerental.exception.DuplicateAccountException if the email is taken */
    SessionUser register(RegisterForm form);

    /** @throws com.vehiclerental.exception.AuthenticationFailedException on a wrong email or password */
    SessionUser login(String email, String password);
}
