package one.microstream.demo.dto;

import java.time.LocalDate;
import java.util.Set;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import one.microstream.demo.domain.Book;

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
    public static UpdateBook from(final Book book)
    {
        return new UpdateBook(
            book.isbn(),
            book.title(),
            book.description(),
            book.pages(),
            book.genres(),
            book.publicationDate()
        );
    }
}
