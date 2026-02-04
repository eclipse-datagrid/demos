package one.microstream.demo.controller;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import one.microstream.demo.DataGenerator;
import one.microstream.demo.MyCache;
import one.microstream.demo.MyCacheKey;
import one.microstream.demo.MyCacheRegionFactory;
import one.microstream.demo.dto.GenerateData;
import one.microstream.demo.dto.InvalidCacheEntry;
import one.microstream.demo.repository.AuthorRepository;
import one.microstream.demo.repository.BookRepository;
import one.microstream.demo.repository.GenreRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * {@link Controller} class for generating data.
 */
@Tag(name = "Data", description = "Endpoints for generating data.")
@Controller("/data")
public class DataController
{
    private final GenreRepository genres;
    private final AuthorRepository authors;
    private final BookRepository books;

    public DataController(
        GenreRepository genres,
        AuthorRepository authors,
        BookRepository books
    )
    {
        this.genres = genres;
        this.authors = authors;
        this.books = books;
    }

    @Operation(summary = "Generate new data")
    @RequestBody(description = "The data generation configuration.")
    @ApiResponse(description = "The data has been generated.")
    @Post("/generate")
    public void generateData(@NonNull @Valid @Body GenerateData config)
    {
        var generator = new DataGenerator(
            genres,
            authors,
            books,
            config.genreConf(),
            config.authorConf(),
            config.bookConf()
        );
        generator.generateData();
    }

    @Post("/invalidate")
    public void invalidateCacheEntry(@NonNull @Valid @Body InvalidCacheEntry entry)
    {
        final var manager = MyCacheRegionFactory.CACHE_MANAGER;
        if (manager == null)
        {
            throw new RuntimeException("Manager has not been initialized by Hibernate yet");
        }
        final var cache = manager.getCache(entry.cacheName());
        if (cache == null)
        {
            System.out.println("Can't invalidate as we don't have a cache named " + entry.cacheName());
        }

        final var timestampsKey = entry.updateTimestampsKey();
        if (timestampsKey != null)
        {
            final var timestampsValue = entry.updateTimestampsValue();
            System.out.println("Received key: " + timestampsKey + ", value: " + timestampsValue);
            cache.receiveTimestampInvalidate(timestampsKey, timestampsValue);
        }
        else
        {
            final byte[] idBytes = Base64.getDecoder().decode(entry.idB64().getBytes(StandardCharsets.UTF_8));
            final var id = MyCache.SERIALIZER.deserialize(idBytes);
            cache.receiveInvalidate(new MyCacheKey(
                id,
                entry.entityOrRoleName(),
                entry.tenantIdentifier(),
                entry.entityHashCode()
            ));
        }
    }
}
