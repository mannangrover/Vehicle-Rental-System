package com.vehiclerental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Number and date come from the invoices table; the amounts are worked out from the booking and payments. */
public class Invoice {

    private String invoiceNumber;
    private long bookingId;
    private LocalDateTime issuedAt;
    private BigDecimal rentalAmount;
    private BigDecimal depositAmount;
    private BigDecimal lateFee;
    private BigDecimal damageFee;
    /** Total charges: rental + late fee + damage fee. The deposit is held, not charged. */
    private BigDecimal totalCharges;
    /** Payments minus refunds. */
    private BigDecimal netPaid;
    /** What the customer still has to pay now. Never negative. */
    private BigDecimal amountDue;

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public long getBookingId() { return bookingId; }
    public void setBookingId(long bookingId) { this.bookingId = bookingId; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public BigDecimal getRentalAmount() { return rentalAmount; }
    public void setRentalAmount(BigDecimal rentalAmount) { this.rentalAmount = rentalAmount; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public BigDecimal getLateFee() { return lateFee; }
    public void setLateFee(BigDecimal lateFee) { this.lateFee = lateFee; }
    public BigDecimal getDamageFee() { return damageFee; }
    public void setDamageFee(BigDecimal damageFee) { this.damageFee = damageFee; }
    public BigDecimal getTotalCharges() { return totalCharges; }
    public void setTotalCharges(BigDecimal totalCharges) { this.totalCharges = totalCharges; }
    public BigDecimal getNetPaid() { return netPaid; }
    public void setNetPaid(BigDecimal netPaid) { this.netPaid = netPaid; }
    public BigDecimal getAmountDue() { return amountDue; }
    public void setAmountDue(BigDecimal amountDue) { this.amountDue = amountDue; }

    public boolean isPaidInFull() {
        return amountDue.signum() == 0;
    }
}
