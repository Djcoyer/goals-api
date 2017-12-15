package com.example.goalsapi.controllers;

import com.example.goalsapi.models.CustomerReservationAggregate;
import com.example.goalsapi.models.Reservation;
import com.example.goalsapi.services.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping(path = "/reservations")
public class ReservationController {

    private ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    //region GET

    @RequestMapping(path = "", method = RequestMethod.GET)
    public ArrayList<Reservation> getReservations() {
        return reservationService.getReservations();
    }

    @RequestMapping(path = "/{reservationId}", method = RequestMethod.GET)
    public Reservation getReservation(@PathVariable("reservationId") String reservationId) {
        return reservationService.getReservation(reservationId);
    }

    @RequestMapping(path = "/user/{userId}", method = RequestMethod.GET)
    public ArrayList<CustomerReservationAggregate>
    getCustomerReservations(@PathVariable("userId") String userId) {
        return reservationService.getUserReservations(userId);
    }

    @RequestMapping(path = "/user/{userId}/active", method = RequestMethod.GET)
    public ArrayList<CustomerReservationAggregate>
    getActiveCustomerReservations(@PathVariable("userId") String userId) {
        return reservationService.getActiveUserReservations(userId);
    }

    //endregion

    //region POST

    @RequestMapping(path = "", method = RequestMethod.POST)
    public Reservation addReservation(@RequestBody Reservation reservation) {
        return reservationService.addReservation(reservation);
    }

    //endregion

    //region DELETE
    @RequestMapping(path = "/{reservationId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteReservation(@PathVariable("reservationId") String reservationId) {
        reservationService.deleteReservation(reservationId);
    }

    //endregion

    //region PATCH

    @RequestMapping(path = "/{reservationId}/return", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.OK)
    public void endReservation(@PathVariable("reservationId") String reservationId) {
        reservationService.endReservation(reservationId);
    }

    //endregion
}
