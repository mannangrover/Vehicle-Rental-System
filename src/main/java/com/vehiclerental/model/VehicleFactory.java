package com.vehiclerental.model;

public final class VehicleFactory {

    private VehicleFactory() {
    }

    public static Vehicle create(VehicleType type) {
        return switch (type) {
            case CAR -> new Car();
            case BIKE -> new Bike();
            case SUV -> new Suv();
        };
    }
}
