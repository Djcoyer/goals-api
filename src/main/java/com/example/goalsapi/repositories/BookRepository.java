package com.example.goalsapi.repositories;

import com.example.goalsapi.models.dao.BookDao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends MongoRepository<BookDao, String>{


    @Query(fields = "{'title':  1, 'author': 1}", value = "{'bookId': ?0}")
    BookDao getBookAggregateInfo(String bookId);

    List<BookDao> getAllByAvailable();

    @Query(value = "{'bookId': ?0, 'available': true}", fields = "{'available':1}")
    BookDao getBookAvailable(String bookId);
}
