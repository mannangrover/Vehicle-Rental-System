package com.vehiclerental.model;

public enum VehicleType {
    CAR("Car"), BIKE("Bike"), SUV("SUV");

    private final String label;

    VehicleType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
