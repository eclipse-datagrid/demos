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
import one.microstream.demo.bean.DataGenerator;
import one.microstream.demo.dto.GenerateData;
import one.microstream.demo.repository.AuthorRepository;
import one.microstream.demo.repository.BookRepository;
import one.microstream.demo.repository.GenreRepository;
import org.eclipse.datagrid.cluster.nodelibrary.types.ClusterFoundation;

/**
 * {@link Controller} class for generating data.
 */
@Tag(name = "Data", description = "Endpoints for generating data.")
@Controller("/data")
public class DataController
{
    private final ClusterFoundation<?> foundation;
    private final GenreRepository genres;
    private final AuthorRepository authors;
    private final BookRepository books;

    public DataController(
        ClusterFoundation<?> foundation,
        GenreRepository genres,
        AuthorRepository authors,
        BookRepository books
    )
    {
        this.foundation = foundation;
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
            foundation,
            genres,
            authors,
            books,
            config.genreConf(),
            config.authorConf(),
            config.bookConf()
        );
        generator.generateData();
    }
}
