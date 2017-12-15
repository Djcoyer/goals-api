package com.example.goalsapi.services;

import com.example.goalsapi.Exceptions.ConflictException;
import com.example.goalsapi.Exceptions.InvalidInputException;
import com.example.goalsapi.Exceptions.NotFoundException;
import com.example.goalsapi.models.Book;
import com.example.goalsapi.models.CustomerReservationAggregate;
import com.example.goalsapi.models.Reservation;
import com.example.goalsapi.models.dao.ReservationDao;
import com.example.goalsapi.repositories.ReservationRepository;
import com.example.goalsapi.transformers.ReservationTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReservationService {

    private ReservationRepository reservationRepository;
    private BookService bookService;
    private UserService userService;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, BookService bookService,
                              UserService userService) {
        this.reservationRepository = reservationRepository;
        this.bookService = bookService;
        this.userService = userService;
    }

    //region GET

    public Reservation getReservation(String reservationId) {
        ReservationDao reservationDao = reservationRepository.findOne(reservationId);
        if (reservationDao == null)
            throw new NotFoundException();
        Reservation reservation = ReservationTransformer.transform(reservationDao);
        return reservation;
    }

    public ArrayList<Reservation> getReservationsByUserId(String userId) {
        List<ReservationDao> reservationDaos = reservationRepository.findAllByUserId(userId);
        ArrayList<Reservation> reservations = new ArrayList<>();
        if (reservationDaos == null || reservationDaos.size() == 0)
            return reservations;
        for (ReservationDao reservationDao : reservationDaos) {
            Reservation reservation = ReservationTransformer.transform(reservationDao);
            reservations.add(reservation);
        }

        return reservations;
    }

    public Reservation getReservationByUserIdAndBookId(String userId, String bookId) {
        ReservationDao reservationDao = reservationRepository.findByUserIdAndBookId(userId, bookId);
        if (reservationDao == null)
            throw new NotFoundException();
        Reservation reservation = ReservationTransformer.transform(reservationDao);
        return reservation;
    }

    public ArrayList<Reservation> getReservations() {
        List<ReservationDao> reservationDaos = reservationRepository.findAll();
        ArrayList<Reservation> reservations = new ArrayList<>();
        if (reservationDaos == null || reservationDaos.size() == 0)
            return reservations;
        for (ReservationDao reservationDao : reservationDaos) {
            Reservation reservation = ReservationTransformer.transform(reservationDao);
            reservations.add(reservation);
        }
        return reservations;
    }

    public ArrayList<CustomerReservationAggregate> getUserReservations(String userId) {
        ArrayList<Reservation> reservations = getReservationsByUserId(userId);
        ArrayList<CustomerReservationAggregate> reservationAggregates = new ArrayList<>();
        if (reservations.size() == 0)
            return reservationAggregates;
        for (Reservation reservation : reservations) {
            String bookId = reservation.getBookId();
            Book book = bookService.getBookAggregateInfo(bookId);
            CustomerReservationAggregate aggregate = new CustomerReservationAggregate();
            aggregate.setTitle(book.getTitle());
            aggregate.setAuthor(book.getAuthor());
            aggregate.setEndDate(reservation.getReservationEndDate());
            aggregate.setBookId(book.getBookId());
            aggregate.setUserId(userId);
            aggregate.setActive(reservation.getReturnedDate() == null);
            aggregate.setStartDate(reservation.getReservationStartDate());
            aggregate.setReservationId(reservation.getReservationId());
            reservationAggregates.add(aggregate);
        }

        return reservationAggregates;
    }

    public CustomerReservationAggregate getUserReservation(String customerId, String bookId) {
        Reservation reservation = getReservationByUserIdAndBookId(customerId, bookId);
        if (reservation == null)
            throw new NotFoundException();
        Book book = bookService.getBook(bookId);
        CustomerReservationAggregate aggregate = new CustomerReservationAggregate();
        aggregate.setAuthor(book.getAuthor());
        aggregate.setBookId(book.getBookId());
        aggregate.setUserId(customerId);
        aggregate.setEndDate(reservation.getReservationEndDate());
        aggregate.setTitle(book.getTitle());
        aggregate.setActive(reservation.getReturnedDate() == null);
        aggregate.setStartDate(reservation.getReservationStartDate());
        aggregate.setReservationId(reservation.getReservationId());
        return aggregate;
    }

    //endregion

    public Reservation addReservation(Reservation reservation) {
        if (reservation.getBookId() == null || reservation.getBookId().equalsIgnoreCase("")
                || reservation.getUserId() == null || reservation.getUserId().equalsIgnoreCase(""))
            throw new InvalidInputException();
        if (!userService.userExists(reservation.getUserId()))
            throw new NotFoundException();
        if(!bookService.bookExists(reservation.getBookId()))
            throw new NotFoundException();
        if(!bookService.isBookAvailable(reservation.getBookId()))
            throw new ConflictException();
        ReservationDao reservationDao = ReservationTransformer.transform(reservation);
        reservationDao.setReservationId(UUID.randomUUID().toString());
        reservationDao.setReservationStartDate(new Date());
        reservationDao.setReservationEndDate(setReservationEndDate());

        bookService.setBookAvailable(reservation.getBookId(), false);

        reservationDao = reservationRepository.save(reservationDao);
        reservation.setReservationId(reservationDao.getReservationId());
        return reservation;
    }

    public void deleteReservation(String reservationId) {
        if(!reservationRepository.exists(reservationId))
            throw new NotFoundException();
        reservationRepository.delete(reservationId);
    }

    //region PATCH

    public void endReservation(String reservationId) {
        ReservationDao reservationDao = reservationRepository.findOne(reservationId);
        if(reservationDao == null)
            throw new NotFoundException();
        String bookId = reservationDao.getBookId();
        bookService.setBookAvailable(bookId, true);
        reservationDao.setReturnedDate(new Date());
        reservationRepository.save(reservationDao);
    }

    //endregion

    //region HELPERS

    private Date setReservationEndDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        Date date = calendar.getTime();
        return date;
    }

    //endregion

}
