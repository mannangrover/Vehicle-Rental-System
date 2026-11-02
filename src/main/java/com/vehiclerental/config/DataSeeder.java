package com.vehiclerental.config;

import com.vehiclerental.dao.BookingDao;
import com.vehiclerental.dao.InvoiceDao;
import com.vehiclerental.dao.PaymentDao;
import com.vehiclerental.dao.UserDao;
import com.vehiclerental.dao.VehicleDao;
import com.vehiclerental.model.Booking;
import com.vehiclerental.model.BookingStatus;
import com.vehiclerental.model.PaymentMethod;
import com.vehiclerental.model.PaymentType;
import com.vehiclerental.model.Role;
import com.vehiclerental.model.User;
import com.vehiclerental.model.Vehicle;
import com.vehiclerental.model.VehicleFactory;
import com.vehiclerental.model.VehicleStatus;
import com.vehiclerental.model.VehicleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/** Adds demo accounts, vehicles and bookings to an empty database. */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserDao userDao;
    private final VehicleDao vehicleDao;
    private final BookingDao bookingDao;
    private final InvoiceDao invoiceDao;
    private final PaymentDao paymentDao;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;
    private int paymentCount;

    public DataSeeder(UserDao userDao, VehicleDao vehicleDao, BookingDao bookingDao, InvoiceDao invoiceDao,
                      PaymentDao paymentDao, BCryptPasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate, Clock clock) {
        this.userDao = userDao;
        this.vehicleDao = vehicleDao;
        this.bookingDao = bookingDao;
        this.invoiceDao = invoiceDao;
        this.paymentDao = paymentDao;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        if (userDao.count() > 0) {
            return;
        }

        addUser("Asha Admin", DemoAccounts.ADMIN.getEmail(), "9800000001", "KA0120180000001",
                DemoAccounts.ADMIN.getPassword(), Role.ADMIN);
        Customer rohan = new Customer(addUser("Rohan Customer", DemoAccounts.CUSTOMER.getEmail(), "9800000002", "KA0120200000002",
                DemoAccounts.CUSTOMER.getPassword(), Role.CUSTOMER), DemoAccounts.CUSTOMER.getEmail());
        Customer priya = addOtherCustomer("Priya Sharma", "priya@roadready.test", "9800000003", "MH1220190000003");
        Customer arjun = addOtherCustomer("Arjun Mehta", "arjun@roadready.test", "9800000004", "DL0820210000004");
        Customer neha = addOtherCustomer("Neha Iyer", "neha@roadready.test", "9800000005", "TN0920220000005");

        Vehicle swift = addVehicle(VehicleType.CAR, "Maruti Suzuki", "Swift", 2023, "KA01MJ4521", "Petrol", "Manual", 5, "1500.00", "200.00", VehicleStatus.AVAILABLE);
        Vehicle i20 = addVehicle(VehicleType.CAR, "Hyundai", "i20", 2024, "KA03NB7810", "Petrol", "Automatic", 5, "1800.00", "240.00", VehicleStatus.AVAILABLE);
        Vehicle city = addVehicle(VehicleType.CAR, "Honda", "City", 2023, "MH12RT3366", "Petrol", "Automatic", 5, "2500.00", "320.00", VehicleStatus.AVAILABLE);
        Vehicle altroz = addVehicle(VehicleType.CAR, "Tata", "Altroz", 2022, "DL8CAF2094", "Diesel", "Manual", 5, "1600.00", "220.00", VehicleStatus.AVAILABLE);
        Vehicle classic = addVehicle(VehicleType.BIKE, "Royal Enfield", "Classic 350", 2024, "KA05KL1180", "Petrol", "Manual", 2, "1200.00", "160.00", VehicleStatus.AVAILABLE);
        Vehicle activa = addVehicle(VehicleType.BIKE, "Honda", "Activa 6G", 2023, "TN09BX6472", "Petrol", "Automatic", 2, "500.00", "70.00", VehicleStatus.AVAILABLE);
        Vehicle pulsar = addVehicle(VehicleType.BIKE, "Bajaj", "Pulsar NS200", 2022, "MH14GH8853", "Petrol", "Manual", 2, "900.00", "120.00", VehicleStatus.AVAILABLE);
        Vehicle ather = addVehicle(VehicleType.BIKE, "Ather", "450X", 2024, "KA51EV3027", "Electric", "Automatic", 2, "800.00", "110.00", VehicleStatus.AVAILABLE);
        Vehicle creta = addVehicle(VehicleType.SUV, "Hyundai", "Creta", 2024, "KA02MZ5519", "Diesel", "Automatic", 5, "2800.00", "360.00", VehicleStatus.AVAILABLE);
        Vehicle thar = addVehicle(VehicleType.SUV, "Mahindra", "Thar", 2023, "GJ01WD7740", "Diesel", "Manual", 4, "3500.00", "450.00", VehicleStatus.AVAILABLE);
        Vehicle innova = addVehicle(VehicleType.SUV, "Toyota", "Innova Crysta", 2022, "TS08UC9215", "Diesel", "Manual", 7, "4000.00", "500.00", VehicleStatus.AVAILABLE);
        addVehicle(VehicleType.SUV, "Tata", "Nexon EV", 2023, "HR26EV1163", "Electric", "Automatic", 5, "2500.00", "320.00", VehicleStatus.MAINTENANCE);
        addVehicle(VehicleType.CAR, "Maruti Suzuki", "Alto", 2017, "KA04AB1290", "Petrol", "Manual", 4, "900.00", "120.00", VehicleStatus.RETIRED);

        LocalDateTime ten = LocalDate.now(clock).atTime(LocalTime.of(10, 0));

        // The demo customer has one booking in every state, by the day and by the hour.
        addCompleted(rohan, swift, ten.minusDays(20), ten.minusDays(17), 0, "0", PaymentMethod.UPI);
        addCompleted(rohan, classic, ten.minusDays(14), ten.minusDays(13), 0, "1500", PaymentMethod.CARD); // balance still due
        addCompleted(rohan, activa, ten.minusDays(9), ten.minusDays(9).plusHours(4), 2, "0", PaymentMethod.UPI); // 2 hours late
        addActive(rohan, creta, ten.minusDays(5), ten.minusDays(2), PaymentMethod.CARD); // overdue
        addConfirmed(rohan, activa, ten.plusDays(1), ten.plusDays(1).plusHours(4), null); // unpaid, by the hour
        addConfirmed(rohan, city, ten.plusDays(3), ten.plusDays(5), PaymentMethod.UPI); // paid
        addConfirmed(rohan, i20, ten.plusDays(5), ten.plusDays(8), null); // unpaid, by the day
        addCancelled(rohan, altroz, ten.plusDays(7), ten.plusDays(9), PaymentMethod.CARD); // refunded

        addCompleted(priya, thar, ten.minusDays(30), ten.minusDays(27), 0, "0", PaymentMethod.CARD);
        addCompleted(priya, pulsar, ten.minusDays(6), ten.minusDays(6).plusHours(6), 0, "0", PaymentMethod.CASH);
        addActive(priya, innova, ten.minusDays(1), ten.plusDays(2), PaymentMethod.UPI); // out and on time

        addCompleted(arjun, creta, ten.minusDays(25), ten.minusDays(18), 0, "0", PaymentMethod.CARD); // long-rental discount
        addCompleted(arjun, ather, ten.minusDays(12), ten.minusDays(11), 0, "750", PaymentMethod.UPI); // small damage
        addConfirmed(arjun, classic, ten.plusDays(10), ten.plusDays(12), PaymentMethod.CARD);

        addCompleted(neha, swift, ten.minusDays(10), ten.minusDays(8), 5, "0", PaymentMethod.CASH); // 5 hours late
        addCancelled(neha, i20, ten.plusDays(12), ten.plusDays(13), null); // cancelled before paying

        log.info("Demo data added");
    }

    private long addUser(String name, String email, String phone, String licenseNo, String password, Role role) {
        User user = new User();
        user.setFullName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setLicenseNo(licenseNo);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        return userDao.insert(user);
    }

    private Vehicle addVehicle(VehicleType type, String brand, String model, int year, String regNumber, String fuelType,
                               String transmission, int seats, String dailyRate, String hourlyRate, VehicleStatus status) {
        Vehicle vehicle = VehicleFactory.create(type);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setModelYear(year);
        vehicle.setRegNumber(regNumber);
        vehicle.setFuelType(fuelType);
        vehicle.setTransmission(transmission);
        vehicle.setSeats(seats);
        vehicle.setDailyRate(new BigDecimal(dailyRate));
        vehicle.setHourlyRate(new BigDecimal(hourlyRate));
        vehicle.setStatus(status);
        vehicle.setId(vehicleDao.insert(vehicle));
        return vehicle;
    }

    private Booking newBooking(long userId, Vehicle vehicle, LocalDateTime startAt, LocalDateTime endAt, BookingStatus status) {
        long hours = ChronoUnit.HOURS.between(startAt, endAt);
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setVehicleId(vehicle.getId());
        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        booking.setDailyRate(vehicle.getDailyRate());
        booking.setHourlyRate(vehicle.getHourlyRate());
        booking.setRentalAmount(vehicle.calculateRentalCost(hours));
        booking.setDepositAmount(vehicle.getSecurityDeposit());
        booking.setLateFee(BigDecimal.ZERO);
        booking.setDamageFee(BigDecimal.ZERO);
        booking.setStatus(status);
        return booking;
    }

    private Customer addOtherCustomer(String name, String email, String phone, String licenseNo) {
        return new Customer(addUser(name, email, phone, licenseNo, "Customer@123", Role.CUSTOMER), email);
    }

    private void addConfirmed(Customer customer, Vehicle vehicle, LocalDateTime startAt, LocalDateTime endAt, PaymentMethod paidBy) {
        Booking booking = newBooking(customer.id(), vehicle, startAt, endAt, BookingStatus.CONFIRMED);
        long id = addBooked(booking, customer, vehicle);
        if (paidBy != null) {
            addFullPayment(id, booking, paidBy, customer);
        }
    }

    private void addCancelled(Customer customer, Vehicle vehicle, LocalDateTime startAt, LocalDateTime endAt, PaymentMethod paidBy) {
        Booking booking = newBooking(customer.id(), vehicle, startAt, endAt, BookingStatus.CANCELLED);
        long id = addBooked(booking, customer, vehicle);
        LocalDateTime cancelledAt = bookedAt(booking).plusHours(3);
        if (paidBy != null) {
            addFullPayment(id, booking, paidBy, customer);
            addRefund(id, booking.getRentalAmount().add(booking.getDepositAmount()), paidBy, cancelledAt);
        }
        addAudit("BOOKING_CANCELLED", "Booking #" + id, customer.email(), cancelledAt);
    }

    private void addActive(Customer customer, Vehicle vehicle, LocalDateTime startAt, LocalDateTime endAt, PaymentMethod paidBy) {
        Booking booking = newBooking(customer.id(), vehicle, startAt, endAt, BookingStatus.ACTIVE);
        booking.setPickedUpAt(startAt);
        booking.setOverdue(endAt.isBefore(LocalDateTime.now(clock)));
        long id = addBooked(booking, customer, vehicle);
        addFullPayment(id, booking, paidBy, customer);
        addAudit("VEHICLE_PICKED_UP", "Booking #" + id, DemoAccounts.ADMIN.getEmail(), startAt);
    }

    /** Settled the way processReturn does: anything paid above the charges goes back to the customer. */
    private void addCompleted(Customer customer, Vehicle vehicle, LocalDateTime startAt, LocalDateTime endAt,
                              long lateHours, String damageFee, PaymentMethod paidBy) {
        Booking booking = newBooking(customer.id(), vehicle, startAt, endAt, BookingStatus.COMPLETED);
        LocalDateTime returnedAt = endAt.plusHours(lateHours);
        booking.setPickedUpAt(startAt);
        booking.setReturnedAt(returnedAt);
        booking.setLateFee(vehicle.calculateLateFee(lateHours));
        booking.setDamageFee(new BigDecimal(damageFee).setScale(2));
        long id = addBooked(booking, customer, vehicle);
        addFullPayment(id, booking, paidBy, customer);

        BigDecimal paid = booking.getRentalAmount().add(booking.getDepositAmount());
        BigDecimal charges = booking.getRentalAmount().add(booking.getLateFee()).add(booking.getDamageFee());
        if (paid.compareTo(charges) > 0) {
            addRefund(id, paid.subtract(charges), paidBy, returnedAt);
        }
        addAudit("VEHICLE_PICKED_UP", "Booking #" + id, DemoAccounts.ADMIN.getEmail(), startAt);
        addAudit("VEHICLE_RETURNED", "Booking #" + id, DemoAccounts.ADMIN.getEmail(), returnedAt);
    }

    private long addBooked(Booking booking, Customer customer, Vehicle vehicle) {
        long id = bookingDao.insert(booking);
        invoiceDao.insert(id, InvoiceDao.invoiceNumber(booking.getStartAt().getYear(), id));
        LocalDateTime bookedAt = bookedAt(booking);
        // The tables fill these in as "now", so set them back to when this booking would have happened
        jdbcTemplate.update("UPDATE bookings SET created_at = ? WHERE id = ?", bookedAt, id);
        jdbcTemplate.update("UPDATE invoices SET issued_at = ? WHERE booking_id = ?", bookedAt, id);
        addAudit("BOOKING_CREATED", "Booking #" + id + ", " + vehicle.getDisplayName(), customer.email(), bookedAt);
        return id;
    }

    private void addFullPayment(long id, Booking booking, PaymentMethod method, Customer customer) {
        LocalDateTime paidAt = bookedAt(booking).plusHours(1);
        addPayment(id, booking.getRentalAmount().add(booking.getDepositAmount()), method, PaymentType.PAYMENT, "PAY-", paidAt);
        addAudit("PAYMENT_MADE", "Booking #" + id, customer.email(), paidAt);
    }

    private void addRefund(long id, BigDecimal amount, PaymentMethod method, LocalDateTime refundedAt) {
        addPayment(id, amount, method, PaymentType.REFUND, "RFD-", refundedAt);
    }

    private void addPayment(long id, BigDecimal amount, PaymentMethod method, PaymentType type, String prefix, LocalDateTime when) {
        long paymentId = paymentDao.insert(id, amount, method, type, prefix + "DEMO" + String.format("%04d", ++paymentCount));
        jdbcTemplate.update("UPDATE payments SET created_at = ? WHERE id = ?", when, paymentId);
    }

    private void addAudit(String action, String details, String actor, LocalDateTime when) {
        jdbcTemplate.update("INSERT INTO audit_logs (action, details, actor, created_at) VALUES (?, ?, ?, ?)",
                action, details, actor, when);
    }

    /** Two days before the start, or a few hours ago for a rental starting soon. */
    private LocalDateTime bookedAt(Booking booking) {
        LocalDateTime twoDaysBefore = booking.getStartAt().minusDays(2);
        LocalDateTime fewHoursAgo = LocalDateTime.now(clock).minusHours(5);
        return twoDaysBefore.isBefore(fewHoursAgo) ? twoDaysBefore : fewHoursAgo;
    }

    private record Customer(long id, String email) {
    }
}
