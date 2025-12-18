package one.microstream.demo;

import one.microstream.demo.domain.Author;

import java.util.Set;
import java.util.stream.Collectors;

public record AuthorNoId(String name, String about, Set<BookNoId> books)
{
    public static AuthorNoId from(Author author)
    {
        var books = author.books().get();
        Set<BookNoId> booksNoId = null;
        if (books != null)
        {
            booksNoId = books.stream().map(BookNoId::from).collect(Collectors.toUnmodifiableSet());
        }
        return new AuthorNoId(
            author.name(),
            author.about(),
            booksNoId
        );
    }
}
