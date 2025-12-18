package one.microstream.demo;

import one.microstream.demo.domain.Book;

import java.time.LocalDate;
import java.util.Set;

public record BookNoId(
    String isbn,
    String title,
    String description,
    int pages,
    Set<String> genres,
    LocalDate publicationDate
)
{
    public static BookNoId from(Book book)
    {
        return new BookNoId(
            book.isbn(),
            book.title(),
            book.description(),
            book.pages(),
            book.genres(),
            book.publicationDate()
        );
    }
}
