package com.example.goalsapi.services;

import com.example.goalsapi.Exceptions.NotFoundException;
import com.example.goalsapi.models.Book;
import com.example.goalsapi.models.dao.BookDao;
import com.example.goalsapi.repositories.BookRepository;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class BookServiceTest {

    private BookService bookService;


    @Mock
    private BookRepository bookRepository;

    private Book book;
    private BookDao bookDao;
    private ArrayList<Book>  books;
    private ArrayList<BookDao> bookDaos;

    private String bookId = "5463ea30-b18c-4b06-9ee7-091536bbe067";
    private String badId = "123456789";

    @Before
    public void init() {
        bookService = new BookService(bookRepository);

        bookDao = new BookDao();
        bookDao.setBookId(bookId);
        bookDao.setAvailable(true);
        bookDao.setAuthor("Test Author");
        bookDao.setTitle("Book Title");
        bookDao.setDescription("Test Description");

    }


    //region GET_BOOK
    @Test
    public void getBook_returnsBook_validRequest(){
        //arrange
        when(bookRepository.findOne(bookId)).thenReturn(bookDao);


        //act
        book = bookService.getBook(bookId);

        //assert
        assertNotNull(book);
        assertEquals(bookDao.getAuthor(), book.getAuthor());
        assertEquals(bookDao.getBookId(), book.getBookId());

    }

    @Test(expected = NotFoundException.class)
    public void getBook_throwsNotFound_InvalidId(){
        //arrange
        when(bookRepository.findOne(badId)).thenReturn(null);

        //act
        book = bookService.getBook(badId);

        //assert
    }

    //endregion

    //region GET_BOOKS

    @Test
    public void getBooks_returnsBooks_validRequest(){
        //arrange
        initBookLists();
        when(bookRepository.findAll()).thenReturn(bookDaos);

        //act
        books = bookService.getBooks();

        //assert
        assertNotNull(books);
        assertThat(books.size(), Matchers.not(0));
    }

    @Test
    public void getBooks_returnsEmptyList_noBooks(){
        //arrange
        when(bookRepository.findAll()).thenReturn(new ArrayList<BookDao>());

        //act
        books = bookService.getBooks();

        //assert
        assertNotNull(books);
        assertEquals(0, books.size());
    }

    //endregion


    //region GET_AVAILABLE_BOOKS

    @Test
    public void getAvailableBooks_returnsBooks_validRequest(){
        //arrange
        initBookLists();
        when(bookRepository.getAllByAvailable()).thenReturn(bookDaos);

        //act
        books = bookService.getAvailableBooks();

        //assert
        assertNotNull(books);
        assertThat(books.size(), Matchers.not(0));
    }

    @Test
    public void getAvailableBooks_returnsEmptyList_noBooks(){
        //arrange
        when(bookRepository.getAllByAvailable()).thenReturn(new ArrayList<BookDao>());

        //act
        books = bookService.getAvailableBooks();

        //assert
        assertNotNull(books);
        assertEquals(0, books.size());
    }

    //endregion



    //region HELPERS

    private void initBookLists() {
//        Book book1 = new Book("1", "Test 1", "Test1", "Description1", true);
//        Book book2 = new Book("2", "Test 2", "Test2", "Description2", true);
//        Book book3 = new Book("3", "Test 3", "Test3", "Description3", true);
//
//        books = new ArrayList<Book>();
//        books.add(book1);
//        books.add(book2);
//        books.add(book3);

        BookDao bookDao1 = new BookDao("1", "Test 1", "Test1", "Description1", true);
        BookDao bookDao2 = new BookDao("2", "Test 2", "Test2", "Description2", true);
        BookDao bookDao3 = new BookDao("3", "Test 3", "Test3", "Description3", true);

        bookDaos = new ArrayList<>();
        bookDaos.add(bookDao1);
        bookDaos.add(bookDao2);
        bookDaos.add(bookDao3);
    }



    //endregion
}
