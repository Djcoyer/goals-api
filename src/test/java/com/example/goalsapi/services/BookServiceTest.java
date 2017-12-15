package com.example.goalsapi.services;

import com.example.goalsapi.Exceptions.InvalidInputException;
import com.example.goalsapi.Exceptions.NotFoundException;
import com.example.goalsapi.models.Book;
import com.example.goalsapi.models.dao.BookDao;
import com.example.goalsapi.repositories.BookRepository;
import com.example.goalsapi.transformers.BookTransformer;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
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

    private String description = "Test Description";
    private String title = "Book Title";
    private String author = "Test Author";

    @Before
    public void init() {
        bookService = new BookService(bookRepository);

        bookDao = new BookDao();
        bookDao.setBookId(bookId);
        bookDao.setAvailable(true);
        bookDao.setAuthor(author);
        bookDao.setTitle(title);
        bookDao.setDescription(description);

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

    //region SERVICE_HELPERS

    @Test
    public void isBookAvailable_returnsValue_validRequest(){
        //arrange
        when(bookRepository.getBookAvailable(bookId)).thenReturn(bookDao);
        boolean expected = true;

        //act
        boolean result = bookService.isBookAvailable(bookId);

        //assert
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void bookExists_returnsValue_validRequest(){
        //arrange
        when(bookRepository.exists(bookId)).thenReturn(true);
        boolean expected = true;
        //act
        boolean result = bookService.bookExists(bookId);

        //assert
        assertNotNull(result);
        assertEquals(expected, result);

    }

    @Test
    public void setBookAvailable_runsSuccessfully_validRequest(){
        //arrange
        when(bookRepository.exists(bookId)).thenReturn(true);
        when(bookRepository.findOne(bookId)).thenReturn(bookDao);
        boolean available = false;

        //act
        bookService.setBookAvailable(bookId, available);

        //assert

    }

    @Test(expected = NotFoundException.class)
    public void setBookAvailable_throwsNotFound_badId(){
        //arrange
        when(bookRepository.exists(bookId)).thenReturn(false);
        when(bookRepository.findOne(bookId)).thenReturn(null);

        //act
        bookService.setBookAvailable(bookId, false);
        //assert
    }

    //endregion


    //region UPDATE_BOOK

    @Test
    public void updateBook_returnsBook_validRequest(){
        //arrange
        book = new Book();
        book.setDescription(description);
        book.setTitle(title);
        book.setAuthor(author);

        when(bookRepository.exists(bookId)).thenReturn(true);
        when(bookRepository.findOne(bookId)).thenReturn(bookDao);

        //act
        Book returnedBook = bookService.updateBook(book, bookId);

        //assert
        assertNotNull(returnedBook);
        assertEquals(title, returnedBook.getTitle());
        assertEquals(author, returnedBook.getAuthor());
        assertEquals(bookId, returnedBook.getBookId());
    }

    @Test(expected = NotFoundException.class)
    public void updateBook_throwsNotFound_invalidId(){
        //arrange
        book = new Book();
        book.setDescription(description);
        book.setTitle(title);
        book.setAuthor(author);

        when(bookRepository.exists(bookId)).thenReturn(false);
        when(bookRepository.findOne(bookId)).thenReturn(bookDao);

        //act
        Book returnedBook = bookService.updateBook(book, bookId);

        //assert
    }

    //endregion

    //region DELETE_BOOK

    @Test
    public void deleteBook_runsSuccessfully_validRequest(){
        //arrange
        when(bookRepository.exists(bookId)).thenReturn(true);

        //act
        bookService.deleteBook(bookId);

        //assert
    }

    @Test(expected = NotFoundException.class)
    public void deleteBook_throwsNotFound_invalidId(){
        //arrange
        when(bookRepository.exists(bookId)).thenReturn(false);
        //act
        bookService.deleteBook(bookId);
        //assert
    }

    //endregion

    //region GET_AGGREGATE_INFO

    @Test
    public void getBookAggregateInfo_returnsBook_validRequest(){
        //arrange
        bookDao.setDescription(null);
        when(bookRepository.getBookAggregateInfo(bookId)).thenReturn(bookDao);

        //act
        Book bookAggregate = bookService.getBookAggregateInfo(bookId);
        //assert
        assertNotNull(bookAggregate);
        assertNull(bookAggregate.getDescription());
        assertEquals(title, bookAggregate.getTitle());
    }

    @Test(expected = NotFoundException.class)
    public void getBookAggregateInfo_throwsNotFound_invalidId(){
        //arrange
        when(bookRepository.getBookAggregateInfo(bookId)).thenReturn(null);

        //act
        Book bookAggregate = bookService.getBookAggregateInfo(bookId);

        //assert
    }

    //endregion

    //region ADD_BOOK

    @Test
    public void addBook_returnsBook_validRequest(){
        //arrange
        book = new Book();
        book.setAuthor(author);
        book.setTitle(title);
        book.setDescription(description);

        BookDao testDao = BookTransformer.transform(book);
        testDao.setAvailable(true);

        when(bookRepository.findByAuthorAndTitle(author,title)).thenReturn(null);
        when(bookRepository.save(testDao)).thenReturn(bookDao);

        //act
        Book returnedBook = bookService.addBook(book);

        //assert
        assertNotNull(returnedBook);
        assertNotNull(returnedBook.getBookId());
        assertEquals(author, returnedBook.getAuthor());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void addBook_throwsDataIntegrityViolation_authorAndTitleExist(){
        //arrange
        book = new Book();
        book.setAuthor(author);
        book.setTitle(title);
        book.setDescription(description);

        when(bookRepository.findByAuthorAndTitle(author, title)).thenReturn(bookDao);

        //act
        Book returnedBook = bookService.addBook(book);

        //assert
    }

    @Test(expected = InvalidInputException.class)
    public void addBook_throwsInvalidInput_nullAuthor(){
        //arrange
        book = new Book();
        book.setTitle(title);
        book.setDescription(description);

        //act
        Book returnedBook = bookService.addBook(book);

        //assert
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
