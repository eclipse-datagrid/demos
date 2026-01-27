package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;

@Introspected
@Serdeable
public record GenerateData(
    @NonNull @Valid DataGenerationConfig genreConf,
    @NonNull @Valid DataGenerationConfig authorConf,
    @NonNull @Valid DataGenerationConfig bookConf
)
{
    @Introspected
    @Serdeable
    public record DataGenerationConfig(int count, Long seed)
    {
    }
}
