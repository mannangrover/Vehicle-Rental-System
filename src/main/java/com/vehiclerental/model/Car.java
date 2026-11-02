package com.vehiclerental.model;

import java.math.BigDecimal;

public class Car extends Vehicle {

    @Override
    public VehicleType getType() {
        return VehicleType.CAR;
    }

    @Override
    protected BigDecimal getLongRentalDiscount() {
        return new BigDecimal("0.10");
    }

    @Override
    public BigDecimal getSecurityDeposit() {
        return new BigDecimal("5000.00");
    }

    @Override
    protected BigDecimal getLateFeeMultiplier() {
        return new BigDecimal("1.50");
    }
}
