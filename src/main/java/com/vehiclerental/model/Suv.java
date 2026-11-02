package com.vehiclerental.model;

import java.math.BigDecimal;

public class Suv extends Vehicle {

    @Override
    public VehicleType getType() {
        return VehicleType.SUV;
    }

    @Override
    protected BigDecimal getLongRentalDiscount() {
        return new BigDecimal("0.05");
    }

    @Override
    public BigDecimal getSecurityDeposit() {
        return new BigDecimal("10000.00");
    }

    @Override
    protected BigDecimal getLateFeeMultiplier() {
        return new BigDecimal("2.00");
    }
}
