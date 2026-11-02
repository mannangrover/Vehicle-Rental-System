package com.vehiclerental.scheduler;

import com.vehiclerental.dao.BookingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class OverdueRentalJob {

    private static final Logger log = LoggerFactory.getLogger(OverdueRentalJob.class);

    private final BookingDao bookingDao;
    private final Clock clock;

    public OverdueRentalJob(BookingDao bookingDao, Clock clock) {
        this.bookingDao = bookingDao;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 3_600_000, initialDelay = 10_000)
    public void markOverdueRentals() {
        int count = bookingDao.markOverdue(LocalDateTime.now(clock));
        log.info("Overdue rental check: {} booking(s) newly marked overdue", count);
    }
}
