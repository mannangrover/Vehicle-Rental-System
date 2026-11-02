package com.vehiclerental.service.impl;

import com.vehiclerental.aspect.Audited;
import com.vehiclerental.config.RentalPolicy;
import com.vehiclerental.dao.BookingDao;
import com.vehiclerental.dao.InvoiceDao;
import com.vehiclerental.dao.PaymentDao;
import com.vehiclerental.dao.VehicleDao;
import com.vehiclerental.event.BookingConfirmedEvent;
import com.vehiclerental.exception.AccessDeniedException;
import com.vehiclerental.exception.InvalidBookingException;
import com.vehiclerental.exception.ResourceNotFoundException;
import com.vehiclerental.exception.VehicleNotAvailableException;
import com.vehiclerental.model.Booking;
import com.vehiclerental.model.BookingStatus;
import com.vehiclerental.model.Invoice;
import com.vehiclerental.model.Payment;
import com.vehiclerental.model.PaymentMethod;
import com.vehiclerental.model.PaymentType;
import com.vehiclerental.model.RentalQuote;
import com.vehiclerental.model.Vehicle;
import com.vehiclerental.model.VehicleStatus;
import com.vehiclerental.service.BookingService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingDao bookingDao;
    private final VehicleDao vehicleDao;
    private final InvoiceDao invoiceDao;
    private final PaymentDao paymentDao;
    private final RentalPolicy rentalPolicy;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public BookingServiceImpl(BookingDao bookingDao, VehicleDao vehicleDao, InvoiceDao invoiceDao,
                              PaymentDao paymentDao, RentalPolicy rentalPolicy,
                              ApplicationEventPublisher eventPublisher, Clock clock) {
        this.bookingDao = bookingDao;
        this.vehicleDao = vehicleDao;
        this.invoiceDao = invoiceDao;
        this.paymentDao = paymentDao;
        this.rentalPolicy = rentalPolicy;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    public RentalQuote quote(long vehicleId, LocalDateTime startAt, LocalDateTime endAt) {
        validateTimes(startAt, endAt);
        Vehicle vehicle = findVehicle(vehicleId);
        long hours = rentalHours(startAt, endAt);
        return new RentalQuote(hours, vehicle.calculateRentalCost(hours), vehicle.getSecurityDeposit());
    }

    @Override
    @Audited("BOOKING_CREATED")
    @Transactional
    public Booking createBooking(long userId, long vehicleId, LocalDateTime startAt, LocalDateTime endAt) {
        validateTimes(startAt, endAt);

        // Must come before any other query: this lock is what prevents double-booking.
        if (!vehicleDao.lockById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found");
        }
        Vehicle vehicle = findVehicle(vehicleId);
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            throw new VehicleNotAvailableException("This vehicle is not available for rental");
        }
        if (bookingDao.countConflicts(vehicleId, startAt, endAt, LocalDateTime.now(clock)) > 0) {
            throw new VehicleNotAvailableException("This vehicle is already booked for those times");
        }

        long hours = rentalHours(startAt, endAt);
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setVehicleId(vehicleId);
        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        booking.setDailyRate(vehicle.getDailyRate());
        booking.setHourlyRate(vehicle.getHourlyRate());
        booking.setRentalAmount(vehicle.calculateRentalCost(hours));
        booking.setDepositAmount(vehicle.getSecurityDeposit());
        booking.setLateFee(BigDecimal.ZERO);
        booking.setDamageFee(BigDecimal.ZERO);
        booking.setStatus(BookingStatus.CONFIRMED);
        long bookingId = bookingDao.insert(booking);
        eventPublisher.publishEvent(new BookingConfirmedEvent(bookingId));
        return findBooking(bookingId);
    }

    @Override
    public Booking getById(long bookingId) {
        return findBooking(bookingId);
    }

    @Override
    public Booking getForUser(long bookingId, long userId) {
        Booking booking = findBooking(bookingId);
        checkOwner(booking, userId);
        return booking;
    }

    @Override
    public List<Booking> listForUser(long userId) {
        return bookingDao.findByUser(userId);
    }

    @Override
    public List<Booking> listAll(BookingStatus status) {
        return status == null ? bookingDao.findAll() : bookingDao.findByStatus(status);
    }

    @Override
    @Audited("BOOKING_CANCELLED")
    @Transactional
    public void cancel(long bookingId, long userId) {
        Booking booking = lockBooking(bookingId);
        checkOwner(booking, userId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingException("Only a confirmed booking can be cancelled");
        }
        if (!booking.getStartAt().isAfter(LocalDateTime.now(clock))) {
            throw new InvalidBookingException("A booking can only be cancelled before its start time");
        }
        bookingDao.updateStatus(bookingId, BookingStatus.CANCELLED);
        BigDecimal netPaid = paymentDao.netPaid(bookingId);
        if (netPaid.signum() > 0) {
            refund(bookingId, netPaid);
        }
    }

    @Override
    @Audited("PAYMENT_MADE")
    @Transactional
    public Payment pay(long bookingId, long userId, PaymentMethod method) {
        if (method == null) {
            throw new InvalidBookingException("Choose a payment method");
        }
        Booking booking = lockBooking(bookingId);
        checkOwner(booking, userId);
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.COMPLETED) {
            throw new InvalidBookingException("This booking cannot be paid in its current status");
        }
        BigDecimal amountDue = amountDue(booking, paymentDao.netPaid(bookingId));
        if (amountDue.signum() <= 0) {
            throw new InvalidBookingException("Nothing is due on this booking");
        }
        long paymentId = paymentDao.insert(bookingId, amountDue, method, PaymentType.PAYMENT, newReference("PAY-"));
        return paymentDao.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Override
    @Audited("VEHICLE_PICKED_UP")
    @Transactional
    public void markPickedUp(long bookingId) {
        Booking booking = lockBooking(bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingException("Only a confirmed booking can be picked up");
        }
        LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);
        if (now.isBefore(booking.getStartAt().minusHours(rentalPolicy.getEarlyPickupHours()))) {
            throw new InvalidBookingException("This booking cannot be picked up this long before its start time");
        }
        if (amountDue(booking, paymentDao.netPaid(bookingId)).signum() > 0) {
            throw new InvalidBookingException("This booking must be paid in full before pickup");
        }
        // An overdue rental may still have this vehicle out
        if (bookingDao.countActiveForVehicle(booking.getVehicleId()) > 0) {
            throw new InvalidBookingException("This vehicle has not been returned from its previous rental");
        }
        bookingDao.markPickedUp(bookingId, now);
    }

    @Override
    @Audited("VEHICLE_RETURNED")
    @Transactional
    public void processReturn(long bookingId, LocalDateTime returnedAt, BigDecimal damageFee) {
        Booking booking = lockBooking(bookingId);
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new InvalidBookingException("Only a vehicle that is out on rental can be returned");
        }
        if (returnedAt == null) {
            throw new InvalidBookingException("Enter the return time");
        }
        LocalDateTime returned = returnedAt.truncatedTo(ChronoUnit.MINUTES);
        if (returned.isBefore(booking.getPickedUpAt())) {
            throw new InvalidBookingException("The return time cannot be before the pickup time");
        }
        if (returned.isAfter(LocalDateTime.now(clock))) {
            throw new InvalidBookingException("The return time cannot be in the future");
        }
        if (damageFee == null || damageFee.signum() < 0) {
            throw new InvalidBookingException("Enter a damage fee of zero or more");
        }

        // Rounded to the column's scale first, so the refund below matches what is stored.
        BigDecimal damage = damageFee.setScale(2, RoundingMode.HALF_UP);
        BigDecimal lateFee = findVehicle(booking.getVehicleId()).calculateLateFee(lateHours(booking.getEndAt(), returned));
        bookingDao.markReturned(bookingId, returned, lateFee, damage);

        BigDecimal totalCharges = booking.getRentalAmount().add(lateFee).add(damage);
        BigDecimal netPaid = paymentDao.netPaid(bookingId);
        if (netPaid.compareTo(totalCharges) > 0) {
            refund(bookingId, netPaid.subtract(totalCharges));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> findInvoice(long bookingId) {
        Optional<Invoice> found = invoiceDao.findByBookingId(bookingId);
        if (found.isEmpty()) {
            return found;
        }
        Invoice invoice = found.get();
        Booking booking = findBooking(bookingId);
        BigDecimal netPaid = paymentDao.netPaid(bookingId);
        invoice.setRentalAmount(booking.getRentalAmount());
        invoice.setDepositAmount(booking.getDepositAmount());
        invoice.setLateFee(booking.getLateFee());
        invoice.setDamageFee(booking.getDamageFee());
        invoice.setTotalCharges(totalCharges(booking));
        invoice.setNetPaid(netPaid);
        invoice.setAmountDue(amountDue(booking, netPaid));
        return found;
    }

    @Override
    public List<Payment> listPayments(long bookingId) {
        return paymentDao.findByBooking(bookingId);
    }

    private void validateTimes(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new InvalidBookingException("Choose a pick-up time and a return time");
        }
        if (!isOnTheHour(startAt) || !isOnTheHour(endAt)) {
            throw new InvalidBookingException("Choose whole hours, such as 2:00 pm");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (startAt.isBefore(now)) {
            throw new InvalidBookingException("The pick-up time cannot be in the past");
        }
        if (!endAt.isAfter(startAt)) {
            throw new InvalidBookingException("The return time must be after the pick-up time");
        }
        if (rentalHours(startAt, endAt) > rentalPolicy.getMaxRentalDays() * 24L) {
            throw new InvalidBookingException("A rental can last at most " + rentalPolicy.getMaxRentalDays() + " days");
        }
        if (startAt.isAfter(now.plusDays(rentalPolicy.getMaxAdvanceDays()))) {
            throw new InvalidBookingException(
                    "A rental can start at most " + rentalPolicy.getMaxAdvanceDays() + " days from today");
        }
    }

    private boolean isOnTheHour(LocalDateTime time) {
        return time.equals(time.truncatedTo(ChronoUnit.HOURS));
    }

    private long rentalHours(LocalDateTime startAt, LocalDateTime endAt) {
        return ChronoUnit.HOURS.between(startAt, endAt);
    }

    /** Any part of an hour counts as a full late hour. */
    private long lateHours(LocalDateTime endAt, LocalDateTime returnedAt) {
        long lateMinutes = ChronoUnit.MINUTES.between(endAt, returnedAt);
        return lateMinutes <= 0 ? 0 : (lateMinutes + 59) / 60;
    }

    /** Must be the transaction's first query, so that the re-read after it sees the latest committed row. */
    private Booking lockBooking(long bookingId) {
        if (!bookingDao.lockById(bookingId)) {
            throw new ResourceNotFoundException("Booking not found");
        }
        return findBooking(bookingId);
    }

    private Booking findBooking(long bookingId) {
        return bookingDao.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private Vehicle findVehicle(long vehicleId) {
        return vehicleDao.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

    private void checkOwner(Booking booking, long userId) {
        if (booking.getUserId() != userId) {
            throw new AccessDeniedException("This booking belongs to another customer");
        }
    }

    private BigDecimal totalCharges(Booking booking) {
        return booking.getRentalAmount().add(booking.getLateFee()).add(booking.getDamageFee());
    }

    /** Before return the customer owes rental plus deposit. After return the deposit no longer counts. */
    private BigDecimal amountDue(Booking booking, BigDecimal netPaid) {
        return switch (booking.getStatus()) {
            case COMPLETED -> totalCharges(booking).subtract(netPaid).max(BigDecimal.ZERO);
            case CONFIRMED, ACTIVE -> booking.getRentalAmount().add(booking.getDepositAmount())
                    .subtract(netPaid).max(BigDecimal.ZERO);
            default -> BigDecimal.ZERO;
        };
    }

    /** Sends the money back the same way the customer last paid. */
    private void refund(long bookingId, BigDecimal amount) {
        PaymentMethod method = paymentDao.findLatestPaymentMethod(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking " + bookingId + " has a refund due but no payment"));
        paymentDao.insert(bookingId, amount, method, PaymentType.REFUND, newReference("RFD-"));
    }

    private String newReference(String prefix) {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return prefix + random.toUpperCase(Locale.ROOT);
    }
}
