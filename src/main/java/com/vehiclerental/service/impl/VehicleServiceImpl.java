package com.vehiclerental.service.impl;

import com.vehiclerental.aspect.Audited;
import com.vehiclerental.dao.BookingDao;
import com.vehiclerental.dao.VehicleDao;
import com.vehiclerental.dto.VehicleForm;
import com.vehiclerental.dto.VehicleSearchCriteria;
import com.vehiclerental.exception.InvalidBookingException;
import com.vehiclerental.exception.ResourceNotFoundException;
import com.vehiclerental.model.Vehicle;
import com.vehiclerental.model.VehicleFactory;
import com.vehiclerental.model.VehicleStatus;
import com.vehiclerental.service.VehicleService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleDao vehicleDao;
    private final BookingDao bookingDao;
    private final Clock clock;

    public VehicleServiceImpl(VehicleDao vehicleDao, BookingDao bookingDao, Clock clock) {
        this.vehicleDao = vehicleDao;
        this.bookingDao = bookingDao;
        this.clock = clock;
    }

    @Override
    public List<Vehicle> search(VehicleSearchCriteria criteria) {
        LocalDateTime start = criteria.getStartAt();
        LocalDateTime end = criteria.getEndAt();
        List<Vehicle> vehicles;
        if (start != null && end != null) {
            if (!end.isAfter(start)) {
                throw new InvalidBookingException("The return time must be after the pick-up time");
            }
            vehicles = vehicleDao.findAvailableBetween(start, end, LocalDateTime.now(clock));
        } else {
            vehicles = vehicleDao.findAvailable();
        }
        return vehicles.stream()
                .filter(v -> criteria.getType() == null || v.getType() == criteria.getType())
                .filter(v -> criteria.getMaxDailyRate() == null
                        || v.getDailyRate().compareTo(criteria.getMaxDailyRate()) <= 0)
                .toList();
    }

    @Override
    public List<Vehicle> findAll() {
        return vehicleDao.findAll();
    }

    @Override
    public Vehicle getById(long id) {
        return vehicleDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
    }

    @Override
    @Audited("VEHICLE_CREATED")
    @Transactional
    public long create(VehicleForm form) {
        Vehicle vehicle = fromForm(form);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        try {
            return vehicleDao.insert(vehicle);
        } catch (DuplicateKeyException e) {
            throw new InvalidBookingException("That registration number is already in use");
        }
    }

    @Override
    @Audited("VEHICLE_UPDATED")
    @Transactional
    public void update(long id, VehicleForm form) {
        getById(id);
        Vehicle vehicle = fromForm(form);
        vehicle.setId(id);
        try {
            vehicleDao.update(vehicle);
        } catch (DuplicateKeyException e) {
            throw new InvalidBookingException("That registration number is already in use");
        }
    }

    @Override
    @Audited("VEHICLE_STATUS_CHANGED")
    @Transactional
    public void setStatus(long id, VehicleStatus status) {
        // Same lock as createBooking, so a booking can't slip in while the status changes.
        if (!vehicleDao.lockById(id)) {
            throw new ResourceNotFoundException("Vehicle not found");
        }
        boolean takingOutOfService = status == VehicleStatus.RETIRED || status == VehicleStatus.MAINTENANCE;
        if (takingOutOfService && bookingDao.countActiveForVehicle(id) > 0) {
            throw new InvalidBookingException("This vehicle is out on a rental. Process the return first.");
        }
        vehicleDao.updateStatus(id, status);
    }

    private Vehicle fromForm(VehicleForm form) {
        Vehicle vehicle = VehicleFactory.create(form.getType());
        vehicle.setBrand(form.getBrand().trim());
        vehicle.setModel(form.getModel().trim());
        vehicle.setModelYear(form.getModelYear());
        vehicle.setRegNumber(form.getRegNumber().trim());
        vehicle.setFuelType(form.getFuelType().trim());
        vehicle.setTransmission(form.getTransmission().trim());
        vehicle.setSeats(form.getSeats());
        vehicle.setDailyRate(form.getDailyRate());
        vehicle.setHourlyRate(form.getHourlyRate());
        if (form.getHourlyRate().compareTo(form.getDailyRate()) > 0) {
            throw new InvalidBookingException("The hourly rate cannot be more than the daily rate");
        }
        return vehicle;
    }
}
