package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import one.microstream.demo.domain.Book;
import one.microstream.demo.domain.Genre;

import java.util.Set;
import java.util.UUID;

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
            book.getId(),
            book.getTitle(),
            Genre.getGenreNames(book.getGenres()),
            book.getAuthor().getId()
        );
    }
}
