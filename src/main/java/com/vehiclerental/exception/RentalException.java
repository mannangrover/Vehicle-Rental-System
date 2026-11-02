package com.vehiclerental.exception;

/** Base for errors whose message is safe to show to the user. */
public class RentalException extends RuntimeException {

    public RentalException(String message) {
        super(message);
    }
}
