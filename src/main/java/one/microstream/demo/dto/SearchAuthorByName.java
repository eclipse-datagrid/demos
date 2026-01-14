package one.microstream.demo.dto;

import java.util.UUID;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import one.microstream.demo.domain.Author;

@Serdeable
@Introspected
public record SearchAuthorByName(
    @NonNull UUID id,
    @NonNull @NotBlank String name
)
{
    public static SearchAuthorByName from(final Author author)
    {
        return new SearchAuthorByName(author.id(), author.name());
    }
}
