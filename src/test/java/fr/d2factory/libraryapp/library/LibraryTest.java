package fr.d2factory.libraryapp.library;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.d2factory.libraryapp.book.Book;
import fr.d2factory.libraryapp.book.BookRepository;
import fr.d2factory.libraryapp.member.Member;
import fr.d2factory.libraryapp.member.ResidentMembers;
import fr.d2factory.libraryapp.member.StudentMembers;

public class LibraryTest {
	private Member student;
	private Member resident;
	private BookRepository bookRepository;
	private List<Book> listBooks;
	private float originalWallet;
	
	
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();
	@Before
	public void setup() {
		// TODO instantiate the library and the repository
		student = new StudentMembers(LocalDate.of(2014, Month.JANUARY, 1),100);
		resident = new ResidentMembers(LocalDate.of(2014, Month.JANUARY, 1),50);

		bookRepository = new BookRepository();
		// TODO add some test books (use BookRepository#addBooks)
		// TODO to help you a file called books.json is available in src/test/resources
		try {
			String booksJson = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("books.json"),
					"UTF-8");
			// JSON from file to Object
			Gson gson = new Gson();
			listBooks = gson.fromJson(booksJson, new TypeToken<List<Book>>() {
			}.getType());
			bookRepository.addBooks(listBooks);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void member_can_borrow_a_book_if_book_is_available() {
		Book book = listBooks.get(1);
		Book borrowedBook = bookRepository.borrowBook(book.getIsbn().getIsbnCode(), student, LocalDate.of(2014, Month.JANUARY, 15));
		assertNotNull(borrowedBook);
	}

	@Test 
	public void borrowed_book_is_no_longer_available() {
		Book book = listBooks.get(1);
		bookRepository.borrowBook(book.getIsbn().getIsbnCode(), student, LocalDate.of(2014, Month.JANUARY, 15));		
		Book borrowedBook2= bookRepository.borrowBook(book.getIsbn().getIsbnCode(), student, LocalDate.of(2014, Month.JANUARY, 15));		
		assertNull(borrowedBook2);
	}

	@Test
	public void residents_are_taxed_10cents_for_each_day_they_keep_a_book() {
		Book book = listBooks.get(1);
		originalWallet = resident.getWallet();
		bookRepository.borrowBook(book.getIsbn().getIsbnCode(), resident, LocalDate.of(2014, Month.JANUARY, 15));		
		bookRepository.returnBook(book, resident, 30);
		assertTrue(resident.getWallet()== originalWallet-3);
	}

	@Test
	public void students_pay_10_cents_the_first_30days() {
		Book book = listBooks.get(1);
		originalWallet = student.getWallet();
		bookRepository.borrowBook(book.getIsbn().getIsbnCode(), student, LocalDate.of(2015, Month.FEBRUARY, 15));		
		bookRepository.returnBook(book, student, 20);
		assertTrue(student.getWallet()== originalWallet-2);
		}

	@Test
	public void students_in_1st_year_are_not_taxed_for_the_first_15days() {
		Book book = listBooks.get(1);
		float originalWallet = student.getWallet();
		bookRepository.borrowBook(book.getIsbn().getIsbnCode(), student, LocalDate.of(2014, Month.FEBRUARY, 15));		
		bookRepository.returnBook(book, student, 45);
		assertTrue(student.getWallet()== originalWallet-3);	
		}

	@Test
	public void students_pay_15cents_for_each_day_they_keep_a_book_after_the_initial_30days() {
		Book book = listBooks.get(2);
		originalWallet = student.getWallet();
		bookRepository.borrowBook(book.getIsbn().getIsbnCode(), student, LocalDate.of(2015, Month.FEBRUARY, 15));		
		bookRepository.returnBook(book, student, 40);
		assertTrue(Float.compare(student.getWallet(),(float)(originalWallet-3-(10*0.15)))<=0.000000001);	
		}

	@Test
	public void residents_pay_20cents_for_each_day_they_keep_a_book_after_the_initial_60days() {
		Book book = listBooks.get(2);
		originalWallet = resident.getWallet();
		bookRepository.borrowBook(book.getIsbn().getIsbnCode(), resident, LocalDate.of(2015, Month.FEBRUARY, 15));		
		bookRepository.returnBook(book, resident, 64);
		assertTrue(Float.compare(resident.getWallet(),(float)(originalWallet-6-(4*0.20)))<=0.000000001);	
		}
	@Test(expected = HasLateBooksException.class)
	public void resident_cannot_borrow_book_if_they_have_late_books() {
		Book book = listBooks.get(2);
		originalWallet = resident.getWallet();
		bookRepository.borrowBook(book.getIsbn().getIsbnCode(), resident, LocalDate.of(2015, Month.FEBRUARY, 15));		
		bookRepository.returnBook(book, resident, 61);
		bookRepository.borrowBook(book.getIsbn().getIsbnCode(), resident, LocalDate.of(2015, Month.DECEMBER, 15));		
		exceptionRule.expect(HasLateBooksException.class); 	
		exceptionRule.expectMessage("Member is Late");
	}
	@Test(expected = HasLateBooksException.class)
	public void student_cannot_borrow_book_if_they_have_late_books() {
		Book book = listBooks.get(2);
		originalWallet = student.getWallet();
		bookRepository.borrowBook(book.getIsbn().getIsbnCode(), student, LocalDate.of(2014, Month.FEBRUARY, 15));		
		bookRepository.returnBook(book, student, 32);
		bookRepository.borrowBook(book.getIsbn().getIsbnCode(), student, LocalDate.of(2015, Month.MARCH, 15));		
		exceptionRule.expect(HasLateBooksException.class); 	
		exceptionRule.expectMessage("Member is Late");
	}
}
