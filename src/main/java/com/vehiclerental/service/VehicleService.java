package com.vehiclerental.service;

import com.vehiclerental.dto.VehicleForm;
import com.vehiclerental.dto.VehicleSearchCriteria;
import com.vehiclerental.model.Vehicle;
import com.vehiclerental.model.VehicleStatus;

import java.util.List;

public interface VehicleService {

    /** Customer search: AVAILABLE vehicles only, excluding any already booked for the given dates. */
    List<Vehicle> search(VehicleSearchCriteria criteria);

    /** Admin list: every vehicle whatever its status. */
    List<Vehicle> findAll();

    /** @throws com.vehiclerental.exception.ResourceNotFoundException if there is no such vehicle */
    Vehicle getById(long id);

    /** @throws com.vehiclerental.exception.InvalidBookingException if the registration number is already used */
    long create(VehicleForm form);

    /** @throws com.vehiclerental.exception.InvalidBookingException if the registration number is already used */
    void update(long id, VehicleForm form);

    void setStatus(long id, VehicleStatus status);
}
