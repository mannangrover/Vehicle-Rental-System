package com.vehiclerental.web.admin;

import com.vehiclerental.service.BookingService;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminServletConfig {

    @Bean
    public ServletRegistrationBean<BookingsCsvServlet> bookingsCsvServletRegistration(BookingService bookingService) {
        return new ServletRegistrationBean<>(new BookingsCsvServlet(bookingService), "/admin/reports/bookings.csv");
    }
}
