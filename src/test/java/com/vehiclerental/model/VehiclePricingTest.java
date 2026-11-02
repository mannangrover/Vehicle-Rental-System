package com.vehiclerental.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Plain unit tests: no Spring and no database. Every vehicle here costs 1000 a day or 150 an hour. */
class VehiclePricingTest {

    private static final int DAY = 24;

    private static Vehicle withRates(Vehicle vehicle) {
        vehicle.setDailyRate(new BigDecimal("1000.00"));
        vehicle.setHourlyRate(new BigDecimal("150.00"));
        return vehicle;
    }

    @Test
    void hoursAreChargedAtTheHourlyRate() {
        Vehicle car = withRates(new Car());

        assertThat(car.calculateRentalCost(1)).isEqualByComparingTo("150.00");
        assertThat(car.calculateRentalCost(5)).isEqualByComparingTo("750.00");
    }

    @Test
    void hoursNeverCostMoreThanAFullDay() {
        Vehicle car = withRates(new Car());

        assertThat(car.calculateRentalCost(6)).isEqualByComparingTo("900.00");
        assertThat(car.calculateRentalCost(7)).isEqualByComparingTo("1000.00"); // 1050 is capped at a day
        assertThat(car.calculateRentalCost(23)).isEqualByComparingTo("1000.00");
        assertThat(car.calculateRentalCost(DAY)).isEqualByComparingTo("1000.00");
    }

    @Test
    void daysAndExtraHoursAddUp() {
        Vehicle car = withRates(new Car());

        assertThat(car.calculateRentalCost(DAY + 2)).isEqualByComparingTo("1300.00");
        assertThat(car.calculateRentalCost(2 * DAY + 20)).isEqualByComparingTo("3000.00"); // extra hours capped
    }

    @Test
    void carPricing() {
        Vehicle car = withRates(new Car());

        assertThat(car.calculateRentalCost(6 * DAY)).isEqualByComparingTo("6000.00");
        assertThat(car.calculateRentalCost(7 * DAY)).isEqualByComparingTo("6300.00"); // 10% off 7000
        assertThat(car.getSecurityDeposit()).isEqualByComparingTo("5000.00");
        assertThat(car.calculateLateFee(1)).isEqualByComparingTo("225.00");           // 1.5 times the price
        assertThat(car.calculateLateFee(DAY)).isEqualByComparingTo("1500.00");
        assertThat(car.calculateLateFee(3 * DAY)).isEqualByComparingTo("4500.00");
    }

    @Test
    void bikePricing() {
        Vehicle bike = withRates(new Bike());

        assertThat(bike.calculateRentalCost(6 * DAY)).isEqualByComparingTo("6000.00");
        assertThat(bike.calculateRentalCost(7 * DAY)).isEqualByComparingTo("5950.00"); // 15% off 7000
        assertThat(bike.getSecurityDeposit()).isEqualByComparingTo("1000.00");
        assertThat(bike.calculateLateFee(1)).isEqualByComparingTo("187.50");           // 1.25 times the price
        assertThat(bike.calculateLateFee(DAY)).isEqualByComparingTo("1250.00");
    }

    @Test
    void suvPricing() {
        Vehicle suv = withRates(new Suv());

        assertThat(suv.calculateRentalCost(6 * DAY)).isEqualByComparingTo("6000.00");
        assertThat(suv.calculateRentalCost(7 * DAY)).isEqualByComparingTo("6650.00"); // 5% off 7000
        assertThat(suv.getSecurityDeposit()).isEqualByComparingTo("10000.00");
        assertThat(suv.calculateLateFee(1)).isEqualByComparingTo("300.00");           // 2 times the price
        assertThat(suv.calculateLateFee(DAY)).isEqualByComparingTo("2000.00");
    }

    @Test
    void zeroOrNegativeLateHoursCostNothing() {
        Vehicle[] vehicles = {withRates(new Car()), withRates(new Bike()), withRates(new Suv())};
        for (Vehicle vehicle : vehicles) {
            assertThat(vehicle.calculateLateFee(0)).isEqualByComparingTo("0");
            assertThat(vehicle.calculateLateFee(-2)).isEqualByComparingTo("0");
        }
    }
}
