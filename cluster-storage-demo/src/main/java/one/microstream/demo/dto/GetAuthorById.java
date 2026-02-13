package one.microstream.demo.dto;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import one.microstream.demo.domain.Author;
import one.microstream.demo.domain.Book;

@Serdeable
@Introspected
public record GetAuthorById(
    @NonNull UUID id,
    @NonNull @NotBlank String name,
    @NonNull @NotBlank String about,
    @NonNull Set<GetAuthorByIdBookDto> books
)
{
    public static GetAuthorById from(final Author author)
    {
        return new GetAuthorById(
            author.id(),
            author.name(),
            author.about(),
            GetAuthorByIdBookDto.fromSet(author.books().get())
        );
    }

    @Serdeable
    @Introspected
    public record GetAuthorByIdBookDto(
        @NonNull UUID id,
        @NonNull @NotBlank String isbn,
        @NonNull @NotBlank String title,
        @NonNull @NotBlank String description,
        @Positive int pages,
        @NonNull Set<@NonNull @NotBlank String> genres,
        @NonNull LocalDate publicationDate
    )
    {
        public static Set<GetAuthorByIdBookDto> fromSet(final Set<Book> books)
        {
            return books.stream()
                .map(
                    book -> new GetAuthorByIdBookDto(
                        book.id(),
                        book.isbn(),
                        book.title(),
                        book.description(),
                        book.pages(),
                        book.genres(),
                        book.publicationDate()
                    )
                )
                .collect(Collectors.toUnmodifiableSet());
        }
    }
}
