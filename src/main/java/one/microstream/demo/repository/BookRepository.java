package one.microstream.demo.repository;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.QueryHint;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import one.microstream.demo.domain.Book;
import org.hibernate.Session;
import org.hibernate.jpa.HibernateHints;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for finding and modifying books. All methods hold a cluster-wide read or write lock to ensure consistency
 * with background threads modifying data received from message queues.
 *
 * <p>
 * Note: All results returned from search queries are limited to {@link BookRepository#DEFAULT_PAGE_SIZE}
 */
@Repository
public abstract class BookRepository implements PageableRepository<Book, UUID>
{
    private final EntityManager entityManager;

    public BookRepository(final EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    @QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true")
    @Override
    @NonNull
    public abstract List<Book> findAll();

    @Join("author")
    @Join("genres")
    @Override
    @NonNull
    public abstract Optional<Book> findById(UUID uuid);

    /**
     * Returns a book matching the specified ID.
     *
     * @param id the ID of the book to return
     * @return the book with matching ID
     * @throws MissingBookException if the book could not be found
     */
    public abstract Book getById(final UUID id);

    /**
     * Returns a book matching the specified ISBN.
     *
     * @param isbn the ISBN of the book to return
     * @return the book with matching ISBN
     * @throws MissingBookException if the book could not be found
     */
    @Join("genres")
    @Join("author")
    public abstract Book getByIsbn(final String isbn);

    /**
     * Queries the ID index of the author {@link GigaMap} for the specified ID and returns a list of all books from the
     * author.
     *
     * @param id the ID of the author
     * @return a read-only list of all books from the author
     * @throws MissingAuthorException if the author could not be found
     */
    @Join("genres")
    @Join("author")
    public abstract List<Book> searchByAuthorId(final UUID authorId);

    /**
     * Queries the title index of the books {@link GigaMap} for the specified
     * <code>titleWildcardSearch</code> with a <code>"title:*search*"</code>
     * wildcard query.
     *
     * @param titleWildcardSearch the wildcard search text the title field will be searched with
     * @return a read-only list of all found books for the specified query
     */
    public abstract List<Book> searchByTitleIlike(final String titleIlikeSearchQuery);

    public abstract void deleteAllById(Iterable<UUID> id);

    @Query(
        """
            SELECT DISTINCT b
            FROM Book b
            LEFT JOIN FETCH b.genres g
            WHERE :genreCount = (
                SELECT COUNT(DISTINCT g2.id)
                FROM Book b2
                JOIN b2.genres g2
                WHERE b2.id = b.id AND g2.name IN :genreNames
            )"""
    )
    public abstract List<Book> findAllWithGenres(Iterable<String> genreNames, int genreCount);

    @Transactional
    public Book getByNaturalId(final String isbn)
    {
        return this.entityManager.unwrap(Session.class).bySimpleNaturalId(Book.class).load(isbn);
    }
}
