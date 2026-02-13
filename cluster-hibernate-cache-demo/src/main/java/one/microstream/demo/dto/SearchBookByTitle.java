package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import one.microstream.demo.domain.Book;

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
 */
@Serdeable
@Introspected
public record SearchBookByTitle(
    @NonNull UUID id,
    @NonNull @NotBlank String title,
    @NonNull UUID authorId
)
{
    public static SearchBookByTitle from(final Book book)
    {
        return new SearchBookByTitle(
            book.getId(),
            book.getTitle(),
            book.getAuthor().getId()
        );
    }
}
