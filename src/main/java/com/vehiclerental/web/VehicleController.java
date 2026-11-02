package com.vehiclerental.web;

import com.vehiclerental.dto.VehicleSearchCriteria;
import com.vehiclerental.model.Vehicle;
import com.vehiclerental.model.VehicleType;
import com.vehiclerental.service.VehicleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
public class VehicleController {

    private final VehicleService vehicleService;
    private final Clock clock;

    public VehicleController(VehicleService vehicleService, Clock clock) {
        this.vehicleService = vehicleService;
        this.clock = clock;
    }

    @GetMapping("/vehicles")
    public String list(@ModelAttribute("criteria") VehicleSearchCriteria criteria,
                       BindingResult bindingResult,
                       Model model) {
        List<String> errors = new ArrayList<>();
        if (bindingResult.hasErrors()) {
            errors.add("Some filters could not be read, so they were ignored.");
        }
        if (criteria.getStartAt() != null && criteria.getEndAt() != null
                && !criteria.getEndAt().isAfter(criteria.getStartAt())) {
            errors.add("The return time must be after the pick-up time, so the times were ignored.");
            criteria.setStartAt(null);
            criteria.setEndAt(null);
        }
        model.addAttribute("errors", errors);
        model.addAttribute("pageTitle", "Vehicles");
        model.addAttribute("vehicles", vehicleService.search(criteria));
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("minStart", earliestStart());
        return "vehicles/list";
    }

    @GetMapping("/vehicles/{id}")
    public String detail(@PathVariable long id,
                         @RequestParam(required = false) @DateTimeFormat(pattern = VehicleSearchCriteria.DATE_TIME_PATTERN) LocalDateTime startAt,
                         @RequestParam(required = false) @DateTimeFormat(pattern = VehicleSearchCriteria.DATE_TIME_PATTERN) LocalDateTime endAt,
                         Model model) {
        Vehicle vehicle = vehicleService.getById(id);
        model.addAttribute("pageTitle", vehicle.getDisplayName());
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("startAt", startAt);
        model.addAttribute("endAt", endAt);
        model.addAttribute("minStart", earliestStart());
        return "vehicles/detail";
    }

    /** Rentals start on the hour, so the next whole hour is the soonest choice. */
    private LocalDateTime earliestStart() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.HOURS).plusHours(1);
    }
}
