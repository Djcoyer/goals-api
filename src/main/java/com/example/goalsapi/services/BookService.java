package com.example.goalsapi.services;

import com.example.goalsapi.Exceptions.InvalidInputException;
import com.example.goalsapi.Exceptions.NotFoundException;
import com.example.goalsapi.models.Book;
import com.example.goalsapi.models.dao.BookDao;
import com.example.goalsapi.repositories.BookRepository;
import com.example.goalsapi.transformers.BookTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    //region GET
    public Book getBook(String bookId) {
        BookDao bookDao = bookRepository.findOne(bookId);
        if(bookDao == null)
            throw new NotFoundException();
        Book book = BookTransformer.transform(bookDao);
        return book;
    }

    public Book getBookAggregateInfo(String bookId) {
        BookDao bookDao = bookRepository.getBookAggregateInfo(bookId);
        if(bookDao == null)
            throw new NotFoundException();
        return BookTransformer.transform(bookDao);
    }

    public ArrayList<Book> getBooks() {
        List<BookDao> bookDaos = bookRepository.findAll();
        ArrayList<Book> books = new ArrayList<>();
        if(bookDaos.size() == 0)
            return books;
        for(BookDao bookDao : bookDaos) {
            Book book = BookTransformer.transform(bookDao);
            books.add(book);
        }
        return books;
    }

    public ArrayList<Book> getAvailableBooks() {
        List<BookDao> bookDaos = bookRepository.getAllByAvailable();
        ArrayList<Book> books = new ArrayList<>();
        if(bookDaos.size() == 0)
            return books;
        for(BookDao bookDao : bookDaos) {
            Book book = BookTransformer.transform(bookDao);
            books.add(book);
        }
        return books;
    }

    //endregion

    public Book addBook(Book book) {
        if(book.getAuthor() == null || book.getAuthor().equalsIgnoreCase("")
                || book.getTitle() == null || book.getTitle().equalsIgnoreCase("")
                || book.getDescription() == null || book.getDescription().equalsIgnoreCase("")){
            throw new InvalidInputException();
        }
        if(bookRepository.findByAuthorAndTitle(book.getAuthor(), book.getTitle()) != null)
            throw new DataIntegrityViolationException("Title and author already exist");
        BookDao bookDao = BookTransformer.transform(book);
        bookDao.setAvailable(true);
        bookDao = bookRepository.save(bookDao);
        book.setBookId(bookDao.getBookId());
        book.setAvailable(bookDao.isAvailable());
        return book;
    }

    public void deleteBook(String bookId) {
        if(!bookRepository.exists(bookId))
            throw new NotFoundException();
        bookRepository.delete(bookId);
    }

    public Book updateBook(Book book, String bookId) {
        if(!bookRepository.exists(bookId))
            throw new NotFoundException();
        BookDao bookDao = bookRepository.findOne(bookId);

        String description = book.getDescription();
        String title = book.getTitle();
        String author = book.getAuthor();
        if(description != null && ! description.equalsIgnoreCase(""))
            bookDao.setDescription(description);
        if(title!= null && ! title.equalsIgnoreCase(""))
            bookDao.setTitle(title);
        if(author!= null && ! author.equalsIgnoreCase(""))
            bookDao.setAuthor(author);

        bookRepository.save(bookDao);
        book = BookTransformer.transform(bookDao);
        return book;
    }

    public void setBookAvailable(String bookId, boolean isAvailable) {
        if(!bookRepository.exists(bookId))
            throw new NotFoundException();
        BookDao bookDao = bookRepository.findOne(bookId);
        bookDao.setAvailable(isAvailable);
        bookRepository.save(bookDao);
    }

    //region HELPERS

    public boolean isBookAvailable(String bookId) {
        BookDao bookDao = bookRepository.getBookAvailable(bookId);
        return bookDao.isAvailable();
    }

    public boolean bookExists(String bookId) {
        return bookRepository.exists(bookId);
    }

    //endregion

}
