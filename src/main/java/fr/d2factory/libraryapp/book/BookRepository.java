package fr.d2factory.libraryapp.book;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.d2factory.libraryapp.library.HasLateBooksException;
import fr.d2factory.libraryapp.library.Library;
import fr.d2factory.libraryapp.member.Member;
import fr.d2factory.libraryapp.member.ResidentMembers;
import fr.d2factory.libraryapp.member.StudentMembers;

/**
 * The book repository emulates a database via 2 HashMaps
 */
public class BookRepository implements Library {
	private Map<ISBN, Book> availableBooks = new HashMap<>();
	private Map<Book, LocalDate> borrowedBooks = new HashMap<>();
	/**
	 * add a new book to the available list of books
	 * @param books
	 */
	public void addBooks(List<Book> books) {
		// TODO implement the missing feature
		if (books.size() > 0) {
			for (Book book : books) {
				getAvailableBooks().put(book.getIsbn(), book);
			}
		}
	}
	/**
	 * find a book by the ISBN code in the available list of books
	 * @param isbnCode
	 * @return
	 */
	public Book findBook(long isbnCode) {
		for (Map.Entry<ISBN, Book> entry : availableBooks.entrySet()) {
			if (entry.getKey().getIsbnCode() == isbnCode)
				return entry.getValue();
		}

		return null;
	}
	
	public Map<ISBN, Book> getAvailableBooks() {
		return availableBooks;
	}

	public void setAvailableBooks(Map<ISBN, Book> availableBooks) {
		this.availableBooks = availableBooks;
	}

	public Map<Book, LocalDate> getBorrowedBooks() {
		return borrowedBooks;
	}

	public void setBorrowedBooks(Map<Book, LocalDate> borrowedBooks) {
		this.borrowedBooks = borrowedBooks;
	}
	/**
	 * save a book in the borrowed list of book if it's borrowed by a member
	 * in the same time the same book will be removed from the available list of books
	 * if borrowedAt is null the book will be added to the available list of books
	 * and removed from the borrowed list of books. 
	 * @param book
	 * @param borrowedAt
	 */
	public void saveBookBorrow(Book book, LocalDate borrowedAt) {
		if (book != null) {
			if (borrowedAt != null) {
				getBorrowedBooks().put(book, borrowedAt);
				getAvailableBooks().remove(book.getIsbn(), book);
			} else {
				getBorrowedBooks().remove(book);
				getAvailableBooks().put(book.getIsbn(), book);
			}
		}

	}

	public LocalDate findBorrowedBookDate(Book book) {
		// TODO implement the missing feature
		return getBorrowedBooks().get(book);
	}
	/**
	 * borrow  book if it's available in the available list of books
	 * if the member was late in the last borrowing, he will be banned from borrowing a new book
	 * so it's throwing an Exception of type HasLateBooksException
	 */
	@Override
	public Book borrowBook(long isbnCode, Member member, LocalDate borrowedAt) throws HasLateBooksException {
		Book book = findBook(isbnCode);
		if (!member.isLate()) {
			if (book != null) {
				saveBookBorrow(findBook(isbnCode), borrowedAt);
			}
		} else {
			throw new HasLateBooksException(member);
		}
		return book;
	}
	/**
	 * a member can return a borrowed book, if he is in the first year so he can get 
	 */
	@Override
	public void returnBook(Book book, Member member, int numbersOfDays) {
		int seniority = 0;
		boolean isLate  = false;
		if (member instanceof StudentMembers) {
			seniority = (int) ChronoUnit.DAYS.between(((StudentMembers) member).getAccessDate(),
					getBorrowedBooks().get(book));
			if(numbersOfDays>30)
			isLate =true;
			if (seniority < 365) 
				numbersOfDays -= 15;
			
		}else if(member instanceof ResidentMembers){
			if(numbersOfDays>60)
				isLate =true;
		}
		saveBookBorrow(book, null);
		member.payBook(numbersOfDays,isLate);

	}

}
