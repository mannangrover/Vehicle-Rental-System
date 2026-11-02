package com.vehiclerental.model;

import java.math.BigDecimal;

public class Bike extends Vehicle {

    @Override
    public VehicleType getType() {
        return VehicleType.BIKE;
    }

    @Override
    protected BigDecimal getLongRentalDiscount() {
        return new BigDecimal("0.15");
    }

    @Override
    public BigDecimal getSecurityDeposit() {
        return new BigDecimal("1000.00");
    }

    @Override
    protected BigDecimal getLateFeeMultiplier() {
        return new BigDecimal("1.25");
    }
}
