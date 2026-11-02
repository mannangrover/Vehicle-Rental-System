package com.vehiclerental.model;

public enum BookingStatus {
    CONFIRMED, ACTIVE, COMPLETED, CANCELLED;

    public String getLabel() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
