package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import one.microstream.demo.bean.DataGenerationConfig;

import java.util.Optional;

@Introspected
@Serdeable
public record GenerateData(
    @NonNull @Valid DataGenerationConfigImpl genreConf,
    @NonNull @Valid DataGenerationConfigImpl authorConf,
    @NonNull @Valid DataGenerationConfigImpl bookConf
)
{
    @Introspected
    @Serdeable
    public static class DataGenerationConfigImpl implements DataGenerationConfig
    {
        private final int count;
        private final Long seed;

        public DataGenerationConfigImpl(@Positive  int count, @Nullable Long seed)
        {
            this.count = count;
            this.seed = seed;
        }

        @Override
        public int getCount()
        {
            return count;
        }

        @Override
        public Optional<Long> getSeed()
        {
            return Optional.ofNullable(seed);
        }
    }
}
