package com.vehiclerental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Booking {

    private static final DateTimeFormatter TIME_LABEL = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH);

    private long id;
    private long userId;
    private long vehicleId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private BigDecimal dailyRate;
    private BigDecimal hourlyRate;
    private BigDecimal rentalAmount;
    private BigDecimal depositAmount;
    private BigDecimal lateFee;
    private BigDecimal damageFee;
    private BookingStatus status;
    private boolean overdue;
    private LocalDateTime pickedUpAt;
    private LocalDateTime returnedAt;
    private LocalDateTime createdAt;

    // Filled from joins, for display
    private String vehicleName;
    private String regNumber;
    private VehicleType vehicleType;
    private String customerName;
    private String customerEmail;

    public long getHours() {
        return ChronoUnit.HOURS.between(startAt, endAt);
    }

    /** For example "2 days 3 hours". */
    public String getDurationLabel() {
        long days = getHours() / 24;
        long hours = getHours() % 24;
        String dayText = days == 1 ? "1 day" : days + " days";
        String hourText = hours == 1 ? "1 hour" : hours + " hours";
        if (days == 0) {
            return hourText;
        }
        return hours == 0 ? dayText : dayText + " " + hourText;
    }

    public String getStartLabel() { return label(startAt); }
    public String getEndLabel() { return label(endAt); }
    public String getPickedUpLabel() { return label(pickedUpAt); }
    public String getReturnedLabel() { return label(returnedAt); }

    private String label(LocalDateTime time) {
        return time == null ? "" : TIME_LABEL.format(time);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getVehicleId() { return vehicleId; }
    public void setVehicleId(long vehicleId) { this.vehicleId = vehicleId; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    public BigDecimal getRentalAmount() { return rentalAmount; }
    public void setRentalAmount(BigDecimal rentalAmount) { this.rentalAmount = rentalAmount; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public BigDecimal getLateFee() { return lateFee; }
    public void setLateFee(BigDecimal lateFee) { this.lateFee = lateFee; }
    public BigDecimal getDamageFee() { return damageFee; }
    public void setDamageFee(BigDecimal damageFee) { this.damageFee = damageFee; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }
    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }
    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
}
