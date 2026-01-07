package one.microstream.demo;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.eclipsestore.RootProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import one.microstream.demo.domain.DataRoot;
import one.microstream.demo.dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(rebuildContext = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SmokeTest implements TestPropertyProvider
{
    private static final Logger LOG = LoggerFactory.getLogger(SmokeTest.class);

    @TempDir
    static Path eclipsestoreStorageMainStorageDirectory;
    @TempDir
    static Path appLuceneIndexBookStorageDirectory;

    @Override
    public @NonNull Map<String, String> getProperties()
    {
        return Map.of(
            "eclipsestore.storage.main.storage-directory",
            eclipsestoreStorageMainStorageDirectory.toString(),
            "app.lucene.index.book.storage-directory",
            appLuceneIndexBookStorageDirectory.toString()
        );
    }

    @Inject
    @Client("/")
    HttpClient client;
    @Inject
    RootProvider<DataRoot> rootProvider;

    @Order(1)
    @Test
    void testAllEndpoints()
    {
        var client = this.client.toBlocking();

        // insert genres
        LOG.info("Inserting genres");
        client.exchange(HttpRequest.PUT("/genre/action", null));
        client.exchange(HttpRequest.PUT("/genre/thriller", null));

        // ensure both genres are in the genre list
        List<String> actualGenres = client.retrieve(HttpRequest.GET("/genre"), Argument.listOf(String.class));
        var expectedGenres = Set.of("action", "thriller");
        assertTrue(expectedGenres.containsAll(actualGenres));
        assertTrue(actualGenres.containsAll(expectedGenres));

        // delete one and ensure that the list has been updated
        LOG.info("Deleting genre `thriller`");
        client.exchange(HttpRequest.DELETE("/genre/thriller"));
        actualGenres = client.retrieve(HttpRequest.GET("/genre"), Argument.listOf(String.class));
        assertIterableEquals(List.of("action"), actualGenres);

        // insert author0...3
        LOG.info("Inserting 4 authors");
        var authorsToInsert = new ArrayList<>(TestUtils.generateInsertAuthorNoBooksList(3));
        authorsToInsert.add(new InsertAuthor(
            "AuthorWithBooks",
            "I am an author with books!",
            List.of(
                new InsertAuthor.InsertAuthorBook(
                    "Book1Isbn",
                    "Book1Title",
                    "Book1Description",
                    1,
                    Set.of("action"),
                    LocalDate.of(2011, 1, 1)
                ),
                new InsertAuthor.InsertAuthorBook(
                    "Book2Isbn",
                    "Book2Title",
                    "Book2Description",
                    2,
                    Set.of("action"),
                    LocalDate.of(2012, 2, 2)
                )
            )
        ));
        List<GetAuthorById> authors = client.retrieve(
            HttpRequest.POST("/author", authorsToInsert),
            Argument.listOf(GetAuthorById.class)
        );
        assertEquals(4, authors.size());
        List<SearchBookByTitle> booksAfterAuthorInsert = client.retrieve(
            HttpRequest.GET("/book/author/" + authors.get(3).id()),
            Argument.listOf(SearchBookByTitle.class)
        );
        assertEquals(2, booksAfterAuthorInsert.size());

        // update author 1
        LOG.info("Updating author 1");
        var author1 = authors.get(1);
        client.exchange(HttpRequest.PUT(
            "/author/" + author1.id(),
            new UpdateAuthor("UpdatedAuthor", "I am the updated author!")
        ));
        var expectedAuthor1 =
            new GetAuthorById(author1.id(), "UpdatedAuthor", "I am the updated author!", Set.of());
        var actualAuthor1 = client.retrieve("/author/id/" + author1.id(), GetAuthorById.class);
        assertEquals(expectedAuthor1, actualAuthor1);

        // delete author 2
        LOG.info("Deleting author 2");
        var author2 = authors.get(2);
        client.exchange(HttpRequest.DELETE("/author/" + author2.id()));
        assertEquals(3, rootProvider.root().authors().size());

        // insert 3 books (book 3-5)
        LOG.info("Inserting 3 books");
        var booksToInsert = List.of(
            new InsertBook(
                "Book3Isbn",
                "Book3Title",
                "Book3Description",
                3,
                Set.of("action"),
                LocalDate.of(2013, 3, 3),
                author1.id()
            ),
            new InsertBook(
                "Book4Isbn",
                "Book4Title",
                "Book4Description",
                4,
                Set.of("action"),
                LocalDate.of(2014, 4, 4),
                author1.id()
            ),
            new InsertBook(
                "Booky5Isbn",
                "Book5Title",
                "Book5Description",
                3,
                Set.of("action"),
                LocalDate.of(2015, 5, 5),
                author1.id()
            )
        );

        List<GetBookById> directInsertedBooks = client.retrieve(
            HttpRequest.POST("/book", booksToInsert),
            Argument.listOf(GetBookById.class)
        );
        assertEquals(3, directInsertedBooks.size());
        List<SearchBookByTitle> allBooksAfterInsert = client.retrieve(
            HttpRequest.GET("/book/title?search=b"),
            Argument.listOf(SearchBookByTitle.class)
        );
        assertEquals(5, allBooksAfterInsert.size());

        LOG.info("Updating book 1 and 4");
        // update book 1
        var book1 = allBooksAfterInsert.get(1);
        client.exchange(HttpRequest.PUT(
            "/book/" + book1.id(),
            new UpdateBook(
                "UpdatedBook",
                "I am the updated book!",
                "I am very updated!",
                20,
                Set.of("action"),
                LocalDate.of(2020, 1, 1)
            )
        ));
        var expectedBook1 = new GetBookById(
            book1.id(),
            "UpdatedBook",
            "I am the updated book!",
            "I am very updated!",
            20,
            Set.of("action"),
            LocalDate.of(2020, 1, 1),
            book1.id()
        );
        // update book 4
        var book4 = allBooksAfterInsert.get(4);
        client.exchange(HttpRequest.PUT(
            "/book/" + book4.id(),
            new UpdateBook(
                "UpdatedBook4",
                "I am the updated book!",
                "I am very updated!",
                20,
                Set.of("action"),
                LocalDate.of(2020, 1, 1)
            )
        ));
        var expectedBook4 = new GetBookById(
            book4.id(),
            "UpdatedBook4",
            "I am the updated book!",
            "I am very updated!",
            20,
            Set.of("action"),
            LocalDate.of(2020, 1, 1),
            book4.id()
        );
        var actualBook4 = client.retrieve("/book/id/" + book4.id(), GetBookById.class);
        assertEquals(expectedBook4, actualBook4);

        // delete book 0 and 4
        LOG.info("Deleting book 0 and 4");
        var book0 = allBooksAfterInsert.get(0);
        var book5 = allBooksAfterInsert.get(4);
        client.exchange(HttpRequest.DELETE("/book/batch?ids=%s,%s".formatted(book0.id(), book5.id())));
        assertEquals(3, rootProvider.root().authors().size());
    }

    /**
     * Tests if all data is still available and correct after executing every functionality like inserting, updating and
     * deleting of genres, authors and books.
     */
    @Order(2)
    @Test
    void testDataConsistencyAfterTestAllEndpoints()
    {
        // restart and check if the state is the same as before
        var expectedGenres = Set.of("action");
        var expectedAuthors = List.of(
            new AuthorNoId("Author0", "I am the nr. 0 author.", Set.of()),
            new AuthorNoId("UpdatedAuthor", "I am the updated author!", Set.of()),
            new AuthorNoId(
                "AuthorWithBooks",
                "I am an author with books!",
                Set.of(
                    new BookNoId(
                        "Book1Isbn",
                        "Book1Title",
                        "Book1Description",
                        1,
                        Set.of("action"),
                        LocalDate.of(2010, 1, 1)
                    ),
                    new BookNoId(
                        "Book2Isbn",
                        "Book2Title",
                        "Book2Description",
                        2,
                        Set.of("action"),
                        LocalDate.of(2010, 1, 1)
                    )
                )
            )
        );
        var expectedBooks = List.<BookNoId>of(
            new BookNoId(
                "Book1Isbn",
                "Book1Title",
                "Book1Description",
                1,
                Set.of("action"),
                LocalDate.of(2010, 1, 1)
            ),
            new BookNoId(
                "Book2Isbn",
                "Book2Title",
                "Book2Description",
                2,
                Set.of("action"),
                LocalDate.of(2010, 1, 1)
            )
        );

        var actualGenres = rootProvider.root().genres();
        var actualAuthors = rootProvider.root().authors().query().toList().stream().map(AuthorNoId::from).toList();
        var actualBooks = rootProvider.root().books().query().toList().stream().map(BookNoId::from).toList();

        LOG.info("Checking data consistency");
        assertTrue(expectedGenres.containsAll(actualGenres));
        assertTrue(actualGenres.containsAll(expectedGenres));
        assertIterableEquals(expectedAuthors, actualAuthors);
        assertIterableEquals(expectedBooks, actualBooks);
    }
}
