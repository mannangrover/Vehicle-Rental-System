package com.vehiclerental.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A simulated payment or refund. No real money or gateway is involved. */
public class Payment {

    private long id;
    private long bookingId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentType type;
    private String reference;
    private LocalDateTime createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getBookingId() { return bookingId; }
    public void setBookingId(long bookingId) { this.bookingId = bookingId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public PaymentType getType() { return type; }
    public void setType(PaymentType type) { this.type = type; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
