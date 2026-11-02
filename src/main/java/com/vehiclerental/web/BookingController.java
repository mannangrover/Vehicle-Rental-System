package com.vehiclerental.web;

import com.vehiclerental.dto.VehicleSearchCriteria;
import com.vehiclerental.model.Booking;
import com.vehiclerental.model.BookingStatus;
import com.vehiclerental.model.Invoice;
import com.vehiclerental.model.Payment;
import com.vehiclerental.model.PaymentMethod;
import com.vehiclerental.model.SessionUser;
import com.vehiclerental.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDateTime;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final Clock clock;

    public BookingController(BookingService bookingService, Clock clock) {
        this.bookingService = bookingService;
        this.clock = clock;
    }

    @PostMapping("/vehicles/{id}/book")
    public String book(@PathVariable long id,
                       @RequestParam @DateTimeFormat(pattern = VehicleSearchCriteria.DATE_TIME_PATTERN) LocalDateTime startAt,
                       @RequestParam @DateTimeFormat(pattern = VehicleSearchCriteria.DATE_TIME_PATTERN) LocalDateTime endAt,
                       @SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                       RedirectAttributes redirectAttributes) {
        Booking booking = bookingService.createBooking(user.getId(), id, startAt, endAt);
        redirectAttributes.addFlashAttribute("success", "Booking confirmed. Pay now to complete it.");
        return "redirect:/bookings/" + booking.getId();
    }

    @GetMapping("/bookings")
    public String list(@SessionAttribute(SessionUser.SESSION_KEY) SessionUser user, Model model) {
        model.addAttribute("pageTitle", "My bookings");
        model.addAttribute("bookings", bookingService.listForUser(user.getId()));
        return "bookings/list";
    }

    @GetMapping("/bookings/{id}")
    public String detail(@PathVariable long id,
                         @SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                         Model model) {
        Booking booking = bookingService.getForUser(id, user.getId());
        Invoice invoice = bookingService.findInvoice(id).orElse(null);
        BookingStatus status = booking.getStatus();

        boolean canPay = invoice != null
                && invoice.getAmountDue().signum() > 0
                && (status == BookingStatus.CONFIRMED || status == BookingStatus.COMPLETED);
        boolean canCancel = status == BookingStatus.CONFIRMED
                && booking.getStartAt().isAfter(LocalDateTime.now(clock));

        model.addAttribute("pageTitle", "Booking #" + booking.getId());
        model.addAttribute("booking", booking);
        model.addAttribute("invoice", invoice);
        model.addAttribute("payments", bookingService.listPayments(id));
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("canPay", canPay);
        model.addAttribute("canCancel", canCancel);
        return "bookings/detail";
    }

    @PostMapping("/bookings/{id}/pay")
    public String pay(@PathVariable long id,
                      @RequestParam PaymentMethod method,
                      @SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                      RedirectAttributes redirectAttributes) {
        Payment payment = bookingService.pay(id, user.getId(), method);
        redirectAttributes.addFlashAttribute("success", "Payment recorded. Reference: " + payment.getReference());
        return "redirect:/bookings/" + id;
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable long id,
                         @SessionAttribute(SessionUser.SESSION_KEY) SessionUser user,
                         RedirectAttributes redirectAttributes) {
        bookingService.cancel(id, user.getId());
        redirectAttributes.addFlashAttribute("success", "Booking cancelled. Any payments have been refunded.");
        return "redirect:/bookings/" + id;
    }
}
