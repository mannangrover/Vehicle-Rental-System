package com.vehiclerental.dto;

import com.vehiclerental.model.VehicleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class VehicleForm {

    @NotNull(message = "Choose a vehicle type")
    private VehicleType type;

    @NotBlank(message = "Enter the brand")
    @Size(max = 50, message = "Brand must be 50 characters or fewer")
    private String brand;

    @NotBlank(message = "Enter the model")
    @Size(max = 50, message = "Model must be 50 characters or fewer")
    private String model;

    @NotNull(message = "Enter the model year")
    @Min(value = 2000, message = "Model year must be 2000 or later")
    @Max(value = 2100, message = "Enter a valid model year")
    private Integer modelYear;

    @NotBlank(message = "Enter the registration number")
    @Size(max = 20, message = "Registration number must be 20 characters or fewer")
    private String regNumber;

    @NotBlank(message = "Enter the fuel type")
    @Size(max = 20, message = "Fuel type must be 20 characters or fewer")
    private String fuelType;

    @NotBlank(message = "Enter the transmission")
    @Size(max = 20, message = "Transmission must be 20 characters or fewer")
    private String transmission;

    @NotNull(message = "Enter the number of seats")
    @Min(value = 1, message = "Seats must be at least 1")
    @Max(value = 12, message = "Seats must be 12 or fewer")
    private Integer seats;

    @NotNull(message = "Enter the daily rate")
    @DecimalMin(value = "1.00", message = "Daily rate must be at least 1")
    private BigDecimal dailyRate;

    @NotNull(message = "Enter the hourly rate")
    @DecimalMin(value = "1.00", message = "Hourly rate must be at least 1")
    private BigDecimal hourlyRate;

    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getModelYear() { return modelYear; }
    public void setModelYear(Integer modelYear) { this.modelYear = modelYear; }
    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
}
