package one.microstream.demo.controller;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.format.Format;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import one.microstream.demo.dto.GetBookById;
import one.microstream.demo.dto.InsertBook;
import one.microstream.demo.dto.SearchBookByAuthor;
import one.microstream.demo.dto.SearchBookByGenre;
import one.microstream.demo.dto.SearchBookByTitle;
import one.microstream.demo.dto.UpdateBook;
import one.microstream.demo.exception.InvalidAuthorException;
import one.microstream.demo.exception.InvalidGenreException;
import one.microstream.demo.exception.InvalidIsbnException;
import one.microstream.demo.repository.BookRepository;

/**
 * {@link Controller} class for finding and modifying books.
 * 
 * @see BookRepository
 */
@Tag(name = "Book", description = "Endpoints for querying and modifying books.")
@Controller("/book")
public class BookController
{
    private final BookRepository books;

    public BookController(final BookRepository books)
    {
        this.books = books;
    }

    @Operation(summary = "Insert new books")
    @RequestBody(description = "The list of books to add to the database.")
    @ApiResponse(description = "The books have been added. Returns the newly added books.")
    @ApiResponse(responseCode = "400", description = "One of the books invalid.")
    @Post
    public List<GetBookById> insert(@NonNull @NotEmpty @Body final List<@NonNull @Valid InsertBook> insert)
        throws InvalidAuthorException,
        InvalidIsbnException,
        InvalidGenreException
    {
        return this.books.insert(insert);
    }

    @Operation(summary = "Update an existing book")
    @Parameter(name = "id", description = "The ID of the book to update.")
    @RequestBody(description = "The updated fields of the book.")
    @ApiResponse(description = "The book has been updated.")
    @ApiResponse(
        responseCode = "404",
        description = "The book could not be found."
    )
    @Put("/{id}")
    public void update(@NonNull @PathVariable final UUID id, @NonNull @Valid @Body final UpdateBook update)
    {
        this.books.update(id, update);
    }

    @Operation(summary = "Delete a book")
    @Parameter(name = "id", description = "The ID of the book to delete.")
    @ApiResponse(description = "The book has been deleted.")
    @ApiResponse(
        responseCode = "404",
        description = "The book could not be found."
    )
    @Delete("/{id}")
    public void delete(@NonNull @PathVariable final UUID id)
    {
        this.books.delete(Collections.singletonList(id));
    }

    @Operation(summary = "Delete multiple books")
    @Parameter(name = "ids", description = "The IDs of the books to delete.")
    @ApiResponse(description = "The books have been deleted.")
    @ApiResponse(
        responseCode = "404",
        description = "One of the books could not be found."
    )
    @Delete("/batch")
    public void deleteBatch(@NonNull @NotEmpty @Format("csv") @QueryValue final List<@NonNull UUID> ids)
    {
        this.books.delete(ids);
    }

    @Operation(summary = "Get a book by ID")
    @Parameter(name = "id", description = "The id of the book to get.")
    @ApiResponse(description = "A book with matching id has been found. Returns the book with matching ID.")
    @ApiResponse(
        responseCode = "404",
        description = "The book could not be found."
    )
    @Get("/id/{id}")
    public GetBookById getById(@NonNull @PathVariable final UUID id)
    {
        return this.books.getById(id);
    }

    @Operation(summary = "Get a book by ISBN")
    @Parameter(name = "id", description = "The ISBN of the book to get.")
    @ApiResponse(description = "A book with matching ISBN has been found. Returns the book with matching ISBN.")
    @ApiResponse(
        responseCode = "404",
        description = "The book could not be found."
    )
    @Get("/isbn/{isbn}")
    public GetBookById getByIsbn(@NonNull @NotBlank @PathVariable final String isbn)
    {
        return this.books.getByISBN(isbn);
    }

    @Operation(summary = "Search for books by author")
    @Parameter(name = "id", description = "The ID of the author.")
    @ApiResponse(description = "Returns a list of books from the specified author.")
    @Get("/author/{id}")
    public List<SearchBookByAuthor> searchByAuthor(@NonNull @PathVariable final UUID id)
    {
        return this.books.searchByAuthor(id);
    }

    @Operation(summary = "Search for books by title")
    @Parameter(
        name = "search",
        description = "The search text to search through the book titles. This uses a '*SEARCH-TEXT*' wildcard query."
    )
    @ApiResponse(description = "Returns a list of books that match the title search query.")
    @Get("/title")
    public List<SearchBookByTitle> searchByTitle(@NonNull @NotBlank @QueryValue final String search)
    {
        return this.books.searchByTitle(search);
    }

    @Operation(summary = "Search for books by genre")
    @Parameter(
        name = "genres",
        description = "Comma-separated list (csv) of genres. Every searched book must contain all the specified genres."
    )
    @ApiResponse(description = "Returns a list of books that match the genre search query.")
    @Get("/genre")
    public List<SearchBookByGenre> searchByGenre(
        @NonNull @NotBlank @QueryValue final String genres
    )
    {
        // @Format("csv") doesn't seem to work for single values so we split ourselves
        final Set<String> genresSet = Stream.of(genres.split(","))
            .filter(s -> !s.isBlank())
            .collect(Collectors.toUnmodifiableSet());
        return this.books.searchByGenre(genresSet);
    }
}
