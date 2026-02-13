package one.microstream.demo.controller;

import java.util.Set;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Put;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import one.microstream.demo.exception.InvalidGenreException;
import one.microstream.demo.exception.MissingGenreException;
import one.microstream.demo.repository.GenreRepository;

/**
 * {@link Controller} class for finding and modifying genres.
 * 
 * @see GenreRepository
 */
@Tag(name = "Genre", description = "Endpoints for querying and modifying genres.")
@Controller("/genre")
public class GenreController
{
    private final GenreRepository genres;

    public GenreController(final GenreRepository genres)
    {
        this.genres = genres;
    }

    @Operation(summary = "Insert a new genre")
    @Parameter(name = "genre", description = "The genre to add.")
    @ApiResponse(
        description = "The genre has been added."
    )
    @ApiResponse(responseCode = "400", description = "The genre already exists.")
    @Put("/{genre}")
    public void insert(@NonNull @NotBlank @PathVariable final String genre) throws InvalidGenreException
    {
        this.genres.insert(genre);
    }

    @Operation(summary = "List all genres")
    @ApiResponse(description = "Returns a list of all genres.")
    @Get
    public Set<String> list()
    {
        return this.genres.list();
    }

    @Operation(summary = "Delete a genre")
    @Parameter(name = "genre", description = "The genre to delete.")
    @ApiResponse(
        description = "The genre has been deleted."
    )
    @ApiResponse(responseCode = "404", description = "The genre could not be found.")
    @Delete("/{genre}")
    public void delete(@NonNull @NotBlank @PathVariable final String genre) throws MissingGenreException
    {
        this.genres.delete(genre);
    }
}
