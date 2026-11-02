package com.vehiclerental.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Base class for everything in the fleet. Each subclass decides its own
 * long-rental discount, security deposit and late fee. Rentals are priced in
 * whole hours: full days at the daily rate, then any extra hours at the hourly
 * rate, never charging more than a full day for those extra hours.
 */
public abstract class Vehicle {

    private static final long HOURS_PER_DAY = 24;
    private static final long LONG_RENTAL_HOURS = 7 * HOURS_PER_DAY;

    private long id;
    private String brand;
    private String model;
    private int modelYear;
    private String regNumber;
    private String fuelType;
    private String transmission;
    private int seats;
    private BigDecimal dailyRate;
    private BigDecimal hourlyRate;
    private VehicleStatus status;

    public abstract VehicleType getType();

    /** Fraction taken off rentals of 7 days or more, e.g. 0.10 for 10%. */
    protected abstract BigDecimal getLongRentalDiscount();

    public abstract BigDecimal getSecurityDeposit();

    /** Late time is charged at the normal price times this multiplier. */
    protected abstract BigDecimal getLateFeeMultiplier();

    public BigDecimal calculateRentalCost(long hours) {
        BigDecimal cost = priceForHours(hours);
        if (hours >= LONG_RENTAL_HOURS) {
            cost = cost.multiply(BigDecimal.ONE.subtract(getLongRentalDiscount()));
        }
        return cost.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateLateFee(long lateHours) {
        if (lateHours <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return priceForHours(lateHours).multiply(getLateFeeMultiplier())
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal priceForHours(long hours) {
        BigDecimal fullDays = dailyRate.multiply(BigDecimal.valueOf(hours / HOURS_PER_DAY));
        BigDecimal extraHours = hourlyRate.multiply(BigDecimal.valueOf(hours % HOURS_PER_DAY));
        return fullDays.add(extraHours.min(dailyRate));
    }

    public String getDisplayName() {
        return brand + " " + model;
    }

    public int getLongRentalDiscountPercent() {
        return getLongRentalDiscount().movePointRight(2).intValue();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public int getModelYear() { return modelYear; }
    public void setModelYear(int modelYear) { this.modelYear = modelYear; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { this.status = status; }
}
