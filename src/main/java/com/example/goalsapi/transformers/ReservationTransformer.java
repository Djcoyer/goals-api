package com.example.goalsapi.transformers;

import com.example.goalsapi.models.Reservation;
import com.example.goalsapi.models.dao.ReservationDao;

public class ReservationTransformer {

    public static Reservation transform(ReservationDao reservationDao) {
        Reservation reservation = new Reservation();
        reservation.setBookId(reservationDao.getBookId());
        reservation.setReservationEndDate(reservationDao.getReservationEndDate());
        reservation.setUserId(reservationDao.getUserId());
        reservation.setReservationStartDate(reservationDao.getReservationStartDate());
        reservation.setReservationId(reservationDao.getReservationId());
        reservation.setReturnedDate(reservationDao.getReturnedDate());
        return reservation;
    }

    public static ReservationDao transform(Reservation reservation) {
        ReservationDao reservationDao = new ReservationDao();
        reservationDao.setBookId(reservation.getBookId());
        reservationDao.setReservationEndDate(reservation.getReservationEndDate());
        reservationDao.setUserId(reservation.getUserId());
        reservationDao.setReservationStartDate(reservation.getReservationStartDate());
        reservationDao.setReservationId(reservation.getReservationId());
        reservationDao.setReturnedDate(reservation.getReturnedDate());
        return reservationDao;
    }
}
