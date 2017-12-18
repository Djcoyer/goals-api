package com.example.goalsapi.services;

import com.example.goalsapi.Exceptions.InvalidInputException;
import com.example.goalsapi.Exceptions.NotFoundException;
import com.example.goalsapi.models.Reservation;
import com.example.goalsapi.models.dao.ReservationDao;
import com.example.goalsapi.repositories.ReservationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.omg.CORBA.DynAnyPackage.Invalid;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ReservationServiceTest {

    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookService bookService;

    private String reservationId= "b084c5d5-f6c5-4c52-b36b-d277ecfd47d5";
    private String bookId = "624bb69f-ce24-43a3-b754-4cd53b492365";
    private String userId = "6c9ec18d-ec8f-4cb1-b459-52183c0e50f8";

    private Reservation reservation;
    private ReservationDao reservationDao;
    private Date reservationStartDate;
    private Date reservationEndDate;
    private Date returnedDate;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        reservationService =  new ReservationService(reservationRepository, bookService, userService);

        reservationStartDate = new Date();
        reservationEndDate = reservationService.setReservationEndDate();
        returnedDate = null;
        reservationDao = new ReservationDao(reservationId,bookId,userId,reservationStartDate,reservationEndDate, returnedDate);
    }


    //region GET_RESERVATION

    @Test
    public void getReservation_returnsReservation_validId(){
        //arrange
        when(reservationRepository.findOne(reservationId)).thenReturn(reservationDao);

        //act
        reservation = reservationService.getReservation(reservationId);

        //assert
        assertNotNull(reservation);
        assertEquals(reservationDao.getBookId(), reservation.getBookId());
        assertEquals(reservationDao.getReservationId(), reservation.getReservationId());
    }

    @Test(expected = NotFoundException.class)
    public void getReservation_throwsNotFound_invalidId(){
        //arrange
        when(reservationRepository.findOne(reservationId)).thenReturn(null);

        //act
        reservation = reservationService.getReservation(reservationId);
        //assert
    }


    //endregion

    //region GET_RESERVATIONS_BY_USER_ID

    @Test
    public void getReservationsByUserId_returnsReservations_validUserId(){
        //arrange
        ArrayList<ReservationDao> reservationDaos = initReservations();
        when(reservationRepository.findAllByUserId(userId)).thenReturn(reservationDaos);
        when(userService.userExists(userId)).thenReturn(true);

        //act
        ArrayList<Reservation> reservations = reservationService.getReservationsByUserId(userId);

        //assert
        assertNotNull(reservations);
        assertEquals(reservationDaos.size(), reservations.size());

    }

    @Test
    public void getReservationsByUserId_returnsEmptyArray_noReservationsExist(){
        //arrange
        ArrayList<ReservationDao> reservationDaos = new ArrayList<>();
        when(reservationRepository.findAllByUserId(userId)).thenReturn(reservationDaos);
        when(userService.userExists(userId)).thenReturn(true);

        //act
        ArrayList<Reservation> reservations = reservationService.getReservationsByUserId(userId);

        //assert
        assertNotNull(reservations);
        assertEquals(0, reservations.size());
    }

    @Test(expected = InvalidInputException.class)
    public void getReservationsByUserId_throwsInvalidInput_invalidUserId(){
        //arrange
        when(userService.userExists(userId)).thenReturn(false);
        when(reservationRepository.findAllByUserId(userId)).thenReturn(new ArrayList<>());
        //act
        ArrayList<Reservation> reservations = reservationService.getReservationsByUserId(userId);
        //assert
    }

    //endregion

    //region GET_BY_USER_ID_AND_BOOK_ID

    @Test
    public void getReservationByUserIdAndBookId_returnsReservation_validRequest(){
        //arrange
        when(userService.userExists(userId)).thenReturn(true);
        when(reservationRepository.findByUserIdAndBookId(userId, bookId)).thenReturn(reservationDao);

        //act
        reservation = reservationService.getReservationByUserIdAndBookId(userId,bookId);

        //assert
        assertNotNull(reservation);
        assertEquals(reservationDao.getReservationId(), reservation.getReservationId());
    }

    @Test(expected = InvalidInputException.class)
    public void getReservationByUserIdAndBookId_throwsInvalidInput_invalidUserId(){
        //arrange
        when(userService.userExists(userId)).thenReturn(false);

        //act
        reservation = reservationService.getReservationByUserIdAndBookId(userId,bookId);
        //assert
    }

    @Test(expected = NotFoundException.class)
    public void getReservationByUserIdAndBookId_throwsNotFound_noReservation(){
        //arrange
        when(userService.userExists(userId)).thenReturn(true);
        when(reservationRepository.findByUserIdAndBookId(userId,bookId)).thenReturn(null);

        //act
        reservation = reservationService.getReservationByUserIdAndBookId(userId,bookId);
        //assert
    }

    //endregion

    //region GET_RESERVATIONS


    //e



    //region HELPERS

    private ArrayList<ReservationDao> initReservations() {
        ArrayList<ReservationDao> reservationDaos = new ArrayList<>();
        reservationDaos.add(reservationDao);
        reservationDaos.add(new ReservationDao("reservationId", "bookId", userId,reservationStartDate, reservationEndDate, null));
        return reservationDaos;
    }

    //endregion

}
