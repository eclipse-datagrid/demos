package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import one.microstream.demo.domain.Author;

@Serdeable
@Introspected
public record InsertAuthor(@NonNull @NotBlank String name, @NonNull @NotBlank String about)
{
    public static Author toAuthor(final InsertAuthor insert)
    {
        final var a = new Author();
        a.setName(insert.name);
        a.setAbout(insert.about);
        return a;
    }
}
