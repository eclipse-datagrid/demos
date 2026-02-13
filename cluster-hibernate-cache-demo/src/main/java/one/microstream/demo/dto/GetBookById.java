package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import one.microstream.demo.domain.Book;
import one.microstream.demo.domain.Genre;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

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
public record GetBookById(
    @NonNull UUID id,
    @NonNull @NotBlank String isbn,
    @NonNull @NotBlank String title,
    @NonNull @NotBlank String description,
    @Positive int pages,
    @NonNull Set<@NonNull @NotBlank String> genres,
    @NonNull LocalDate publicationDate,
    @NonNull UUID authorId
)
{
    public static GetBookById from(final Book book)
    {
        return new GetBookById(
            book.getId(),
            book.getIsbn(),
            book.getTitle(),
            book.getDescription(),
            book.getPages(),
            Genre.getGenreNames(book.getGenres()),
            book.getPublicationDate(),
            book.getAuthor().getId()
        );
    }
}
