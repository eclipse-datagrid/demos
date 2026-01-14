package one.microstream.demo.dto;

import java.util.Set;
import java.util.UUID;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import one.microstream.demo.domain.Book;

@Serdeable
@Introspected
public record SearchBookByGenre(
    @NonNull UUID id,
    @NonNull @NotBlank String title,
    @NonNull Set<@NonNull @NotBlank String> genres,
    @NonNull UUID authorId
)
{
    public static SearchBookByGenre from(final Book book)
    {
        return new SearchBookByGenre(
            book.id(),
            book.title(),
            book.genres(),
            book.author().id()
        );
    }
}
