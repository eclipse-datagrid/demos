package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import one.microstream.demo.domain.Author;
import one.microstream.demo.domain.Book;
import one.microstream.demo.domain.Genre;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Serdeable
@Introspected
public record InsertBook(
    @NonNull @NotBlank String isbn,
    @NonNull @NotBlank String title,
    @NonNull @NotBlank String description,
    @Positive int pages,
    @NonNull @NotEmpty Set<@NonNull @NotBlank String> genres,
    @NonNull LocalDate publicationDate,
    @NonNull UUID authorId
)
{
    public Book toBook(final Author author, final Set<Genre> genres)
    {
        final var b = new Book();
        b.setIsbn(isbn);
        b.setTitle(title);
        b.setDescription(description);
        b.setPages(pages);
        b.setGenres(genres);
        b.setPublicationDate(publicationDate);
        b.setAuthor(author);
        return b;
    }
}
