package com.vehiclerental.model;

public enum VehicleStatus {
    AVAILABLE, MAINTENANCE, RETIRED;

    public String getLabel() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
