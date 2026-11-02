package com.vehiclerental.web;

import com.vehiclerental.dto.VehicleSearchCriteria;
import com.vehiclerental.model.Vehicle;
import com.vehiclerental.model.VehicleType;
import com.vehiclerental.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class HomeController {

    private static final int FEATURED_LIMIT = 6;

    private final VehicleService vehicleService;
    private final Clock clock;

    public HomeController(VehicleService vehicleService, Clock clock) {
        this.vehicleService = vehicleService;
        this.clock = clock;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Vehicle> featured = vehicleService.search(new VehicleSearchCriteria()).stream()
                .limit(FEATURED_LIMIT)
                .toList();
        model.addAttribute("pageTitle", "Rent a vehicle");
        model.addAttribute("featured", featured);
        model.addAttribute("vehicleTypes", VehicleType.values());
        model.addAttribute("minStart", LocalDateTime.now(clock).truncatedTo(ChronoUnit.HOURS).plusHours(1));
        return "home";
    }
}
