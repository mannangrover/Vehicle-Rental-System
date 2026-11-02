package com.vehiclerental.model;

import java.math.BigDecimal;

public class RentalQuote {

    private final long hours;
    private final BigDecimal rentalAmount;
    private final BigDecimal depositAmount;

    public RentalQuote(long hours, BigDecimal rentalAmount, BigDecimal depositAmount) {
        this.hours = hours;
        this.rentalAmount = rentalAmount;
        this.depositAmount = depositAmount;
    }

    public long getHours() { return hours; }
    public BigDecimal getRentalAmount() { return rentalAmount; }
    public BigDecimal getDepositAmount() { return depositAmount; }

    public BigDecimal getTotalPayable() {
        return rentalAmount.add(depositAmount);
    }
}
