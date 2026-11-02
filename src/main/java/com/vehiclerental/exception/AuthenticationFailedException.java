package com.vehiclerental.exception;

public class AuthenticationFailedException extends RentalException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
