package com.example.goalsapi.models.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Reservations")
public class ReservationDao {
    @Id
    private String reservationId;
    private String bookId;
    private String userId;
    private Date reservationStartDate;
    private Date reservationEndDate;
    private Date returnedDate;
}
