package com.example.goalsapi.services;

import com.example.goalsapi.Exceptions.ConflictException;
import com.example.goalsapi.Exceptions.InvalidInputException;
import com.example.goalsapi.Exceptions.NotFoundException;
import com.example.goalsapi.models.Book;
import com.example.goalsapi.models.User;
import com.example.goalsapi.models.UserReservationAggregate;
import com.example.goalsapi.models.Reservation;
import com.example.goalsapi.models.dao.ReservationDao;
import com.example.goalsapi.repositories.ReservationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mockingDetails;
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

    private ArrayList<UserReservationAggregate> aggregates;
    private UserReservationAggregate aggregate;
    private String title = "Test Book";
    private String author = "Test Author";
    private Book book;




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

    @Test
    public void getReservations_returnsReservations_validRequest(){
        //arrange
        ArrayList<ReservationDao> reservationDaos = initReservations();
        when(reservationRepository.findAll()).thenReturn(reservationDaos);


        //act
        ArrayList<Reservation>  reservations = reservationService.getReservations();

        //assert
        assertNotNull(reservations);
        assertEquals(reservationDaos.size(), reservations.size());
    }

    @Test
    public void getReservations_returnsEmptyArrayList_noReservations(){
        //arrange
        when(reservationRepository.findAll()).thenReturn(null);

        //act
        ArrayList<Reservation> reservations = reservationService.getReservations();

        //assert
        assertNotNull(reservations);
        assertEquals(0, reservations.size());
    }

    //endregion

    //region GET_USER_RESERVATIONS

    @Test
    public void getUserReservations_returnsReservationAggregates_validRequest(){
        //arrange
        initAggregates();
        ArrayList<ReservationDao> reservations = initReservations();
        when(bookService.getBookAggregateInfo(Mockito.anyString())).thenReturn(book);
        when(userService.userExists(userId)).thenReturn(true);
        when(reservationRepository.findAllByUserId(userId)).thenReturn(reservations);
        //act

        ArrayList<UserReservationAggregate> returnedAggregates = reservationService.getUserReservations(userId);

        //assert
        assertNotNull(returnedAggregates);
        assertEquals(reservations.size(), returnedAggregates.size());
    }

    @Test
    public void getUserReservations_returnsEmptyList_noReservations(){
        //arrange
        when(userService.userExists(userId)).thenReturn(true);
        when(reservationRepository.findAllByUserId(userId)).thenReturn(null);

        //act
        ArrayList<UserReservationAggregate> returnedAggregates = reservationService.getUserReservations(userId);

        //assert
        assertNotNull(returnedAggregates);
        assertEquals(0, returnedAggregates.size());
    }


    //endregion

    //region ADD_RESERVATION

    @Test
    public void addReservation_returnsReservation_validRequest(){
        //arrange
        reservation = new Reservation(null,bookId,userId,reservationStartDate,reservationEndDate,null);
        when(userService.userExists(userId)).thenReturn(true);
        when(bookService.bookExists(bookId)).thenReturn(true);
        when(bookService.isBookAvailable(bookId)).thenReturn(true);

        //act

        Reservation returnedReservation = reservationService.addReservation(reservation);

        //assert
        assertNotNull(returnedReservation);
        assertEquals(bookId, returnedReservation.getBookId());
        assertNotNull(returnedReservation.getReservationId());
    }

    @Test(expected = InvalidInputException.class)
    public void addReservation_throwsInvalidInput_nullBookId(){
        //arrange
        reservation = new Reservation(null, "", userId, reservationStartDate, reservationEndDate, null);
        when(userService.userExists(userId)).thenReturn(true);
        when(bookService.bookExists(bookId)).thenReturn(true);
        when(bookService.isBookAvailable(bookId)).thenReturn(true);

        //act
        Reservation returnedReservation  = reservationService.addReservation(reservation);

        //assert
    }

    @Test(expected = NotFoundException.class)
    public void addReservation_throwsNotFound_userNotExists(){
        //arrange
        reservation = new Reservation(null, bookId,userId,reservationStartDate,reservationEndDate,null);
        when(userService.userExists(userId)).thenReturn(false);
        when(bookService.bookExists(bookId)).thenReturn(true);
        when(bookService.isBookAvailable(bookId)).thenReturn(true);

        //act
        Reservation returnedReservation = reservationService.addReservation(reservation);

        //assert
    }

    @Test(expected = NotFoundException.class)
    public void addReservation_throwsNotFound_bookNotExists(){
        //arrange
        reservation = new Reservation(null, bookId,userId,reservationStartDate,reservationEndDate,null);
        when(userService.userExists(userId)).thenReturn(true);
        when(bookService.bookExists(bookId)).thenReturn(false);
        when(bookService.isBookAvailable(bookId)).thenReturn(true);

        //act
        Reservation returnedReservation = reservationService.addReservation(reservation);

        //assert
    }

    @Test(expected = ConflictException.class)
    public void addReservation_throwsConflict_bookNotAvailable(){
        //arrange
        reservation = new Reservation(null, bookId,userId,reservationStartDate,reservationEndDate,null);
        when(userService.userExists(userId)).thenReturn(true);
        when(bookService.bookExists(bookId)).thenReturn(true);
        when(bookService.isBookAvailable(bookId)).thenReturn(false);

        //act
        Reservation returnedReservation = reservationService.addReservation(reservation);

        //assert
    }


    //endregion

    //region GET_USER_RESERVATION

    @Test
    public void getUserReservation_returnsAggregate_validRequest(){
        //arrange
        book = new Book(bookId,title,author,null, true);
        when(userService.userExists(userId)).thenReturn(true);
        when(bookService.bookExists(bookId)).thenReturn(true);
        when(reservationRepository.findByUserIdAndBookId(userId,bookId)).thenReturn(reservationDao);
        when(bookService.getBook(bookId)).thenReturn(book);

        //act
        UserReservationAggregate aggregate = reservationService.getUserReservation(userId,bookId);

        //assert

        assertNotNull(aggregate);
        assertEquals(reservationDao.getReservationId(), aggregate.getReservationId());
        assertEquals(book.getTitle(), aggregate.getTitle());
    }

    //endregion

    //region DELETE_RESERVATION

    @Test
    public void deleteReservation_runsSuccessfully_validRequest(){
        //arrange
        when(reservationRepository.exists(reservationId)).thenReturn(true);

        //act
        reservationService.deleteReservation(reservationId);

        //assert
    }

    @Test(expected = NotFoundException.class)
    public void deleteReservation_throwsNotFound_invalidId(){
        //arrange
        when(reservationRepository.exists(reservationId)).thenReturn(false);
        //act
        reservationService.deleteReservation(reservationId);
        //assert
    }


    //endregion

    //region END_RESERVATION

    @Test
    public void endReservation_runsSuccessfully_validRequest(){
        //arrange
        when(reservationRepository.exists(reservationId)).thenReturn(true);
        when(reservationRepository.findOne(reservationId)).thenReturn(reservationDao);

        //act
        reservationService.endReservation(reservationId);

        //assert
    }

    @Test(expected = NotFoundException.class)
    public void endReservation_throwsNotFound_invalidId(){
        //arrange
        when(reservationRepository.exists(reservationId)).thenReturn(false);
        when(reservationRepository.findOne(reservationId)).thenReturn(null);
        //act
        reservationService.endReservation(reservationId);
        //assert
    }

    //endregion

    //region DELETE_RESERVATIONS_BOOK_ID

    @Test
    public void deleteReservationsByBookId_runsSuccessfully_validRequest(){
        //arrange

        //act
        reservationService.deleteReservationsByBookId(bookId);

        //assert
    }

    @Test(expected = InvalidInputException.class)
    public void deleteReservationsByBookId_throwsInvalidInput_emptyBookId(){
        //arrange

        //act
        reservationService.deleteReservationsByBookId("");
        //assert
    }

    //endregion


    //region HELPERS

    private ArrayList<ReservationDao> initReservations() {
        ArrayList<ReservationDao> reservationDaos = new ArrayList<>();
        reservationDaos.add(reservationDao);
        reservationDaos.add(new ReservationDao("reservationId", "bookId", userId,reservationStartDate, reservationEndDate, null));
        return reservationDaos;
    }

    private void initAggregates() {
        aggregates = new ArrayList<>();
        aggregate = new UserReservationAggregate(userId,bookId,reservationId,title,author,
                reservationStartDate,reservationEndDate,true);
        aggregates.add(aggregate);
        book = new Book(bookId,title,author,null,false);
    }

    //endregion

}
