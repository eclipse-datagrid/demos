package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import one.microstream.demo.domain.Book;
import one.microstream.demo.domain.Genre;

import java.time.LocalDate;
import java.util.Set;
import java.util.function.Function;

/**
 * @param id              the unique identifier
 * @param isbn            the isbn identifier
 * @param title           the title of the book
 * @param description     the description of the book which can usually be found
 *                        on the back
 * @param pages           how many pages the book has
 * @param genres          the genres of the book
 * @param publicationDate when the book was published
 * @param author          the author of the book
 */
@Serdeable
@Introspected
public record UpdateBook(
    @NonNull @NotBlank String isbn,
    @NonNull @NotBlank String title,
    @NonNull @NotBlank String description,
    @Positive int pages,
    @NonNull @NotEmpty Set<@NonNull @NotBlank String> genres,
    @NonNull LocalDate publicationDate
)
{
    public Book toBook(final Function<Set<String>, Set<Genre>> genreConverter)
    {
        final var b = new Book();
        b.setIsbn(isbn);
        b.setTitle(title);
        b.setDescription(description);
        b.setPages(pages);
        b.setGenres(genreConverter.apply(genres));
        b.setPublicationDate(publicationDate);
        return b;
    }
}
