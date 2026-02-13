package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import one.microstream.demo.domain.Author;
import one.microstream.demo.domain.Book;
import one.microstream.demo.domain.Genre;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
            author.getId(),
            author.getName(),
            author.getAbout(),
            GetAuthorByIdBookDto.fromSet(author.getBooks())
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
                        book.getId(),
                        book.getIsbn(),
                        book.getTitle(),
                        book.getDescription(),
                        book.getPages(),
                        Genre.getGenreNames(book.getGenres()),
                        book.getPublicationDate()
                    )
                )
                .collect(Collectors.toUnmodifiableSet());
        }
    }
}
