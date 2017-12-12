package com.example.goalsapi.controllers;

import com.example.goalsapi.models.Book;
import com.example.goalsapi.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping(path = "/books")
public class BookController {

    private BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }


    //region GET

    @RequestMapping(path="", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ArrayList<Book> getBooks() {
        return bookService.getBooks();
    }

    @RequestMapping(path = "/available", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ArrayList<Book> getAvailableBooks() {
        return bookService.getAvailableBooks();
    }

    @RequestMapping(path = "/{bookId}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Book getBook(@PathVariable("bookId")String bookId) {
        return bookService.getBook(bookId);
    }

    //endregion

    @RequestMapping(path = "", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public Book addBook(@RequestBody Book book) {
        return bookService.addBook(book);
    }

    @RequestMapping(path = "/{bookId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void deleteBook(@PathVariable("bookId") String bookId) {
        bookService.deleteBook(bookId);
    }

    @RequestMapping(path = "/{bookId}", method = RequestMethod.PATCH)
    @ResponseStatus(HttpStatus.OK)
    public Book updateBook(@PathVariable("bookId")String bookId, @RequestBody Book book) {
        return bookService.updateBook(book, bookId);
    }


}
