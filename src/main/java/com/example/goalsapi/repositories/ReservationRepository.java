package com.example.goalsapi.repositories;

import com.example.goalsapi.models.dao.ReservationDao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends MongoRepository<ReservationDao, String>{

    List<ReservationDao> findAllByUserId(String customerId);

    ReservationDao findByUserIdAndBookId(String customerId, String bookId);

    @Query(value="{'reservationEndDate':?0, 'bookId': ?1}")
    ReservationDao existsByDateAndBookId(Date reservationEndDate, String bookId);

    @Query(value = "{'reservationId': ?0}", fields="{'bookId':1}")
    ReservationDao getBookIdByreservationId(String reservationId);

    @Query(value="{'returnedDate':null, 'userId':?0}")
    List<ReservationDao> findAllActiveByUserId(String userId);
}
