package one.microstream.demo;

import one.microstream.demo.domain.Author;

import java.util.List;

public record AuthorNoId(String name, String about, List<BookNoId> books)
{
    public static AuthorNoId from(Author author)
    {
        var books = author.books().get();
        List<BookNoId> booksNoId = null;
        if (books != null)
        {
            booksNoId = books.stream().map(BookNoId::from).sorted((a, b) -> a.isbn().compareTo(b.isbn())).toList();
        }
        return new AuthorNoId(
            author.name(),
            author.about(),
            booksNoId
        );
    }
}
