package one.microstream.demo.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

@Serdeable
@Introspected
public record InsertAuthor(
    @NonNull @NotBlank String name,
    @NonNull @NotBlank String about,
    @Nullable List<@NonNull @Valid InsertAuthorBook> books
)
{
    @Serdeable
    @Introspected
    public record InsertAuthorBook(
        @NonNull @NotBlank String isbn,
        @NonNull @NotBlank String title,
        @NonNull @NotBlank String description,
        @Positive int pages,
        @NonNull @NotEmpty Set<@NonNull @NotBlank String> genres,
        @NonNull LocalDate publicationDate
    )
    {
    }
}
