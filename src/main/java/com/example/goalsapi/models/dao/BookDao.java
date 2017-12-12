package com.example.goalsapi.models.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Books")
public class BookDao {
    @Id
    private String bookId;
    private String title;
    private String author;
    private String description;
    private boolean available;
}

