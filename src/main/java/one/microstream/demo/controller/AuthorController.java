package one.microstream.demo.controller;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.format.Format;
import io.micronaut.http.annotation.*;
import io.micronaut.transaction.annotation.Transactional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import one.microstream.demo.domain.Author;
import one.microstream.demo.dto.GetAuthorById;
import one.microstream.demo.dto.InsertAuthor;
import one.microstream.demo.dto.SearchAuthorByName;
import one.microstream.demo.dto.UpdateAuthor;
import one.microstream.demo.repository.AuthorRepository;

import java.util.List;
import java.util.UUID;

/**
 * {@link Controller} class for finding and modifying authors.
 *
 * @see AuthorRepository
 */
@Tag(name = "Author", description = "Endpoints for querying and modifying authors.")
@Controller("/author")
public class AuthorController
{
    private final AuthorRepository authors;

    public AuthorController(final AuthorRepository authors)
    {
        this.authors = authors;
    }

    @Operation(summary = "Insert new authors")
    @RequestBody(description = "The list of authors to add to the database.")
    @ApiResponse(description = "The authors have been added. Returns the newly added authors.")
    @ApiResponse(
        responseCode = "400",
        description = "One of the books from an author is invalid."
    )
    @Post
    public List<Author> insert(@NonNull @NotEmpty @Body final List<@NonNull @Valid InsertAuthor> insert)
    {
        return this.authors.saveAll(insert.stream().map(InsertAuthor::toAuthor).toList());
    }

    @Operation(summary = "Update an existing author")
    @Parameter(name = "id", description = "The ID of the author to update.")
    @RequestBody(description = "The updated fields of the author.")
    @ApiResponse(description = "The author has been updated.")
    @ApiResponse(
        responseCode = "404",
        description = "The author could not be found."
    )
    @Put("/{id}")
    public void update(
        @NonNull @PathVariable final UUID id,
        @NonNull @Valid @Body final UpdateAuthor update
    )
    {
        this.authors.updateAuthorInfo(id, update.name(), update.about());
    }

    @Operation(summary = "Delete an author")
    @Parameter(name = "id", description = "The ID of the author to delete.")
    @ApiResponse(description = "The author has been deleted.")
    @ApiResponse(
        responseCode = "404",
        description = "The author could not be found."
    )
    @Delete("/{id}")
    public void delete(@NonNull @PathVariable final UUID id)
    {
        this.authors.deleteById(id);
    }

    @Operation(summary = "Delete multiple authors")
    @Parameter(name = "ids", description = "The IDs of the authors to delete.")
    @ApiResponse(description = "The authors have been deleted.")
    @ApiResponse(
        responseCode = "404",
        description = "One of the authors could not be found."
    )
    @Delete("/batch")
    public void deleteBatch(@NonNull @NotEmpty @Format("csv") @QueryValue final List<@NonNull UUID> ids)
    {
        this.authors.deleteAllById(ids);
    }

    @Operation(summary = "Get an author by ID")
    @Parameter(name = "id", description = "The ID of the author to get.")
    @ApiResponse(description = "An author with matching ID has been found. Returns the author with matching ID.")
    @ApiResponse(
        responseCode = "404",
        description = "The author could not be found."
    )
    @Get("/id/{id}")
    @Transactional(readOnly = true)
    public GetAuthorById getById(@NonNull @PathVariable final UUID id)
    {
        return this.authors.findById(id).map(GetAuthorById::from).orElse(null);
    }

    @Operation(summary = "Search for authors by name")
    @Parameter(
        name = "search",
        description = "The search text to search through the author names. This uses a 'contains'-search ignoring case."
    )
    @ApiResponse(description = "Returns a list of authors that match the name search query.")
    @Get("/name")
    public List<SearchAuthorByName> searchByName(@NonNull @NotBlank @QueryValue final String search)
    {
        return this.authors.searchByNameIlike("%" + search + "%").stream().map(SearchAuthorByName::from).toList();
    }
}
