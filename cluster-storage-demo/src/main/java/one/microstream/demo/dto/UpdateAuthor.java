package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
@Introspected
public record UpdateAuthor(
    @NonNull @NotBlank String name,
    @NonNull @NotBlank String about
)
{
}
