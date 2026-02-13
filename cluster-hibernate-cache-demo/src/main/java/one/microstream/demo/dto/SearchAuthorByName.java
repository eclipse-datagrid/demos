package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import one.microstream.demo.domain.Author;

import java.util.UUID;

@Serdeable
@Introspected
public record SearchAuthorByName(@NonNull UUID id, @NonNull @NotBlank String name)
{
    public static SearchAuthorByName from(final Author author)
    {
        return new SearchAuthorByName(author.getId(), author.getName());
    }
}
