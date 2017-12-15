package com.example.goalsapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerReservationAggregate {
    private String userId;
    private String bookId;
    private String reservationId;
    private String title;
    private String author;
    private Date startDate;
    private Date endDate;
    private boolean isActive;
}
