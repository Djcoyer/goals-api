package com.example.goalsapi.transformers;

import com.example.goalsapi.Exceptions.InternalServerException;
import com.example.goalsapi.models.Book;
import com.example.goalsapi.models.dao.BookDao;

public class BookTransformer {

    public static Book transform(BookDao bookDao) {
        Book book = new Book();
        try{
            book.setAuthor(bookDao.getAuthor());
            book.setBookId(bookDao.getBookId());
            book.setTitle(bookDao.getTitle());
            book.setDescription(bookDao.getDescription());
            book.setAvailable(bookDao.isAvailable());
            return book;
        }
        catch(Exception e) {
            throw new InternalServerException();
        }
    }

    public static BookDao transform(Book book) {
        try{
            BookDao bookDao = new BookDao();
            bookDao.setAuthor(book.getAuthor());
            bookDao.setBookId((book.getBookId() == null ? "" : book.getBookId()));
            bookDao.setTitle(book.getTitle());
            bookDao.setDescription(book.getDescription());
            bookDao.setAvailable(book.isAvailable());
            return bookDao;
        } catch(Exception e){
            throw new InternalServerException();
        }
    }
}
