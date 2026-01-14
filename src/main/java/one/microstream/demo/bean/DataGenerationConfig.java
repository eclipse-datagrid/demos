package one.microstream.demo.bean;

import io.micronaut.context.annotation.EachProperty;
import jakarta.validation.constraints.Positive;

import java.util.Optional;

@EachProperty("app.data.generation")
public interface DataGenerationConfig
{
    @Positive
    int getCount();

    Optional<Long> getSeed();
}
