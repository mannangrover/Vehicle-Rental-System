package com.vehiclerental.config;

/** Limits on bookings. Defined as a bean in rental-policy.xml. */
public class RentalPolicy {

    private int maxRentalDays;
    private int maxAdvanceDays;
    private int earlyPickupHours;

    /** Bean life cycle hook, called by Spring after the properties are set. */
    public void validate() {
        if (maxRentalDays < 1 || maxAdvanceDays < 0 || earlyPickupHours < 0) {
            throw new IllegalStateException("Invalid rental policy in rental-policy.xml");
        }
    }

    public int getMaxRentalDays() { return maxRentalDays; }
    public void setMaxRentalDays(int maxRentalDays) { this.maxRentalDays = maxRentalDays; }
    public int getMaxAdvanceDays() { return maxAdvanceDays; }
    public void setMaxAdvanceDays(int maxAdvanceDays) { this.maxAdvanceDays = maxAdvanceDays; }
    public int getEarlyPickupHours() { return earlyPickupHours; }
    public void setEarlyPickupHours(int earlyPickupHours) { this.earlyPickupHours = earlyPickupHours; }
}
