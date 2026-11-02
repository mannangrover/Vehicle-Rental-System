package com.vehiclerental.web.admin;

import com.vehiclerental.config.RentalPolicy;
import com.vehiclerental.dto.VehicleForm;
import com.vehiclerental.dto.VehicleSearchCriteria;
import com.vehiclerental.exception.InvalidBookingException;
import com.vehiclerental.model.Booking;
import com.vehiclerental.model.BookingStatus;
import com.vehiclerental.model.Invoice;
import com.vehiclerental.model.Vehicle;
import com.vehiclerental.model.VehicleStatus;
import com.vehiclerental.service.BookingService;
import com.vehiclerental.service.DashboardService;
import com.vehiclerental.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Admin pages. The login filter has already checked that the user is an admin. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final int AUDIT_LOG_LIMIT = 100;

    private final BookingService bookingService;
    private final VehicleService vehicleService;
    private final DashboardService dashboardService;
    private final RentalPolicy rentalPolicy;
    private final Clock clock;

    public AdminController(BookingService bookingService, VehicleService vehicleService,
                           DashboardService dashboardService, RentalPolicy rentalPolicy, Clock clock) {
        this.bookingService = bookingService;
        this.vehicleService = vehicleService;
        this.dashboardService = dashboardService;
        this.rentalPolicy = rentalPolicy;
        this.clock = clock;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("stats", dashboardService.getStats());
        return "admin/dashboard";
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) BookingStatus status, Model model) {
        model.addAttribute("pageTitle", "Manage bookings");
        model.addAttribute("bookings", bookingService.listAll(status));
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("selectedStatus", status);
        return "admin/bookings";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetail(@PathVariable long id, Model model) {
        Booking booking = bookingService.getById(id);
        Invoice invoice = bookingService.findInvoice(id).orElse(null);
        LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime pickupOpensAt = booking.getStartAt().minusHours(rentalPolicy.getEarlyPickupHours());

        boolean awaitingPayment = invoice == null || !invoice.isPaidInFull();
        boolean startsLater = now.isBefore(pickupOpensAt);
        boolean canPickUp = booking.getStatus() == BookingStatus.CONFIRMED && !awaitingPayment && !startsLater;

        model.addAttribute("pageTitle", "Booking #" + id);
        model.addAttribute("booking", booking);
        model.addAttribute("invoice", invoice);
        model.addAttribute("payments", bookingService.listPayments(id));
        model.addAttribute("now", now);
        model.addAttribute("earlyPickupHours", rentalPolicy.getEarlyPickupHours());
        model.addAttribute("awaitingPayment", awaitingPayment);
        model.addAttribute("startsLater", startsLater);
        model.addAttribute("canPickUp", canPickUp);
        if (booking.getStatus() == BookingStatus.ACTIVE) {
            Vehicle vehicle = vehicleService.getById(booking.getVehicleId());
            model.addAttribute("lateFeePerHour", vehicle.calculateLateFee(1));
            model.addAttribute("lateFeePerDay", vehicle.calculateLateFee(24));
        }
        return "admin/booking-detail";
    }

    @PostMapping("/bookings/{id}/pickup")
    public String pickup(@PathVariable long id, RedirectAttributes redirect) {
        bookingService.markPickedUp(id);
        redirect.addFlashAttribute("success", "Booking #" + id + " marked as picked up.");
        return "redirect:/admin/bookings/" + id;
    }

    @PostMapping("/bookings/{id}/return")
    public String processReturn(@PathVariable long id,
                                @RequestParam @DateTimeFormat(pattern = VehicleSearchCriteria.DATE_TIME_PATTERN) LocalDateTime returnedAt,
                                @RequestParam(defaultValue = "0") BigDecimal damageFee,
                                RedirectAttributes redirect) {
        if (returnedAt.isAfter(LocalDateTime.now(clock))) {
            throw new InvalidBookingException("The return time cannot be in the future.");
        }
        if (damageFee.signum() < 0) {
            throw new InvalidBookingException("The damage fee cannot be negative.");
        }
        bookingService.processReturn(id, returnedAt, damageFee);
        redirect.addFlashAttribute("success", "Return recorded for booking #" + id + ".");
        return "redirect:/admin/bookings/" + id;
    }

    @GetMapping("/vehicles")
    public String vehicles(Model model) {
        model.addAttribute("pageTitle", "Fleet");
        model.addAttribute("vehicles", vehicleService.findAll());
        model.addAttribute("vehicleStatuses", VehicleStatus.values());
        return "admin/vehicles";
    }

    @GetMapping("/vehicles/new")
    public String newVehicle(Model model) {
        model.addAttribute("form", new VehicleForm());
        return showVehicleForm(model, false, null);
    }

    @PostMapping("/vehicles")
    public String createVehicle(@Valid @ModelAttribute("form") VehicleForm form, BindingResult result,
                                Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("errors", errorMessages(result));
            return showVehicleForm(model, false, null);
        }
        try {
            vehicleService.create(form);
        } catch (InvalidBookingException e) {
            model.addAttribute("error", e.getMessage());
            return showVehicleForm(model, false, null);
        }
        redirect.addFlashAttribute("success", "Vehicle added.");
        return "redirect:/admin/vehicles";
    }

    @GetMapping("/vehicles/{id}/edit")
    public String editVehicle(@PathVariable long id, Model model) {
        model.addAttribute("form", toForm(vehicleService.getById(id)));
        return showVehicleForm(model, true, id);
    }

    @PostMapping("/vehicles/{id}")
    public String updateVehicle(@PathVariable long id, @Valid @ModelAttribute("form") VehicleForm form,
                                BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("errors", errorMessages(result));
            return showVehicleForm(model, true, id);
        }
        try {
            vehicleService.update(id, form);
        } catch (InvalidBookingException e) {
            model.addAttribute("error", e.getMessage());
            return showVehicleForm(model, true, id);
        }
        redirect.addFlashAttribute("success", "Vehicle updated.");
        return "redirect:/admin/vehicles";
    }

    @PostMapping("/vehicles/{id}/status")
    public String changeVehicleStatus(@PathVariable long id, @RequestParam VehicleStatus status,
                                      RedirectAttributes redirect) {
        vehicleService.setStatus(id, status);
        redirect.addFlashAttribute("success", "Vehicle status changed to " + status + ".");
        return "redirect:/admin/vehicles";
    }

    @GetMapping("/audit")
    public String audit(Model model) {
        model.addAttribute("pageTitle", "Audit log");
        model.addAttribute("logs", dashboardService.recentAuditLogs(AUDIT_LOG_LIMIT));
        return "admin/audit";
    }

    private String showVehicleForm(Model model, boolean editing, Long vehicleId) {
        model.addAttribute("pageTitle", editing ? "Edit vehicle" : "Add vehicle");
        model.addAttribute("editing", editing);
        model.addAttribute("vehicleId", vehicleId);
        return "admin/vehicle-form";
    }

    private VehicleForm toForm(Vehicle vehicle) {
        VehicleForm form = new VehicleForm();
        form.setType(vehicle.getType());
        form.setBrand(vehicle.getBrand());
        form.setModel(vehicle.getModel());
        form.setModelYear(vehicle.getModelYear());
        form.setRegNumber(vehicle.getRegNumber());
        form.setFuelType(vehicle.getFuelType());
        form.setTransmission(vehicle.getTransmission());
        form.setSeats(vehicle.getSeats());
        form.setDailyRate(vehicle.getDailyRate());
        form.setHourlyRate(vehicle.getHourlyRate());
        return form;
    }

    private List<String> errorMessages(BindingResult result) {
        List<String> messages = new ArrayList<>();
        for (FieldError error : result.getFieldErrors()) {
            // A binding failure's default message is a raw Java conversion error, so replace it
            if (error.isBindingFailure()) {
                messages.add("Check the value entered for " + error.getField() + ".");
            } else {
                messages.add(error.getDefaultMessage());
            }
        }
        return messages;
    }
}
