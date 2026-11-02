package com.vehiclerental.dto;

import com.vehiclerental.model.VehicleType;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** All fields are optional. Times only filter when both are given. */
public class VehicleSearchCriteria {

    /** The format a browser's date and time input sends. */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm";

    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime startAt;
    @DateTimeFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime endAt;
    private VehicleType type;
    private BigDecimal maxDailyRate;

    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }
    public BigDecimal getMaxDailyRate() { return maxDailyRate; }
    public void setMaxDailyRate(BigDecimal maxDailyRate) { this.maxDailyRate = maxDailyRate; }
}
