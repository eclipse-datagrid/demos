package one.microstream.demo.dto;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

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
}
