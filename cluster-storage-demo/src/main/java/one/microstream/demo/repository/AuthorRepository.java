package one.microstream.demo.repository;

import java.util.*;

import io.micronaut.eclipsestore.RootProvider;
import jakarta.inject.Singleton;
import one.microstream.demo.domain.Author;
import one.microstream.demo.domain.Book;
import one.microstream.demo.domain.DataRoot;
import one.microstream.demo.dto.GetAuthorById;
import one.microstream.demo.dto.InsertAuthor;
import one.microstream.demo.dto.InsertAuthor.InsertAuthorBook;
import one.microstream.demo.dto.SearchAuthorByName;
import one.microstream.demo.dto.UpdateAuthor;
import one.microstream.demo.exception.InvalidGenreException;
import one.microstream.demo.exception.InvalidIsbnException;
import one.microstream.demo.exception.MissingAuthorException;
import org.eclipse.datagrid.cluster.nodelibrary.types.ClusterLockScope;
import org.eclipse.serializer.concurrency.LockedExecutor;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.store.storage.types.StorageManager;

/**
 * Repository for finding and modifying authors. All methods hold a cluster-wide
 * read or write lock to ensure consistency with background threads modifying
 * data received from message queues.
 *
 * <p>
 * Note: All results returned from search queries are limited to
 * {@link AuthorRepository#DEFAULT_PAGE_SIZE}
 */
@Singleton
public class AuthorRepository extends ClusterLockScope
{
    private static final int DEFAULT_PAGE_SIZE = 512;

    private final List<Author> authors;
    private final List<Book> books;
    private final Set<String> genres;

    private final StorageManager storageManager;

    public AuthorRepository(
        final LockedExecutor executor,
        final RootProvider<DataRoot> rootProvider,
        final StorageManager storageManager
    )
    {
        super(executor);
        final var root = rootProvider.root();
        this.authors = root.authors();
        this.books = root.books();
        this.genres = root.genres();
        this.storageManager = storageManager;
    }

    /**
     * Adds the specified authors to the authors {@link List} and stores it.
     *
     * @param insert the authors to add
     * @return a read-only list of the added authors
     * @throws InvalidIsbnException  if a duplicate ISBN was found
     * @throws InvalidGenreException if a genre could not be found from the
     *                               specified books
     */
    public List<GetAuthorById> insert(final List<InsertAuthor> insert)
        throws InvalidIsbnException,
        InvalidGenreException
    {
        final var returnDtos = new ArrayList<GetAuthorById>(insert.size());

        this.write(() ->
        {
            this.validateInsert(insert);

            boolean modifiedBooks = false;

            for (final var insertAuthor : insert)
            {
                final var author = new Author(
                    UUID.randomUUID(),
                    insertAuthor.name(),
                    insertAuthor.about(),
                    Lazy.Reference(new HashSet<>())
                );
                returnDtos.add(GetAuthorById.from(author));

                List<Book> authorBooks = null;
                if (insertAuthor.books() != null)
                {
                    authorBooks = insertAuthor.books()
                        .stream()
                        .map(
                            b -> new Book(
                                UUID.randomUUID(),
                                b.isbn(),
                                b.title(),
                                b.description(),
                                b.pages(),
                                b.genres(),
                                b.publicationDate(),
                                author
                            )
                        )
                        .toList();
                    author.books().get().addAll(authorBooks);
                }

                this.authors.add(author);

                if (authorBooks != null)
                {
                    this.books.addAll(authorBooks);
                    modifiedBooks = true;
                }
            }

            if (!insert.isEmpty())
            {
                this.storageManager.store(this.authors);

                if (modifiedBooks)
                {
                    this.storageManager.store(this.books);
                }
            }
        });

        return Collections.unmodifiableList(returnDtos);
    }

    /**
     * Updates the author with the specified values by replacing it and stores the
     * authors {@link List}.
     *
     * @param id     the ID of the author to update
     * @param update the new values for the author
     * @throws MissingAuthorException if no author could be found for the specified
     *                                ID
     */
    public void update(final UUID id, final UpdateAuthor update) throws MissingAuthorException
    {
        this.write(() ->
        {
            int index = -1;
            for (int i = 0; i < this.authors.size(); i++)
            {
                final var author = this.authors.get(i);
                if (author.id().equals(id))
                {
                    index = i;
                    break;
                }
            }

            if (index == -1)
            {
                throw new MissingAuthorException(id);
            }

            final var oldAuthor = this.authors.get(index);
            this.authors.set(index, new Author(id, update.name(), update.about(), oldAuthor.books()));
            this.storageManager.store(this.authors);
        });
    }

    /**
     * Removes the books with the specified IDs from the books {@link List} and
     * stores it.
     *
     * @param ids the IDs of the books to remove
     * @throws MissingAuthorException if an author could not be found for one of the
     *                                specified IDs
     */
    public void delete(final Iterable<UUID> ids) throws MissingAuthorException
    {
        this.write(() ->
        {
            final var cachedAuthors = new ArrayList<Author>();
            for (final UUID id : ids)
            {
                // ensure authors exist
                cachedAuthors.add(this.authors.stream()
                    .filter(a -> a.id().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new MissingAuthorException(id)));
            }
            if (!cachedAuthors.isEmpty())
            {
                for (final var author : cachedAuthors)
                {
                    for (final var book : author.books().get())
                    {
                        this.books.remove(book);
                    }
                    this.authors.remove(author);
                }
                this.storageManager.storeAll(this.books, this.authors);
            }
        });
    }

    /**
     * Returns an author matching the specified ID.
     *
     * @param id the ID of the author to return
     * @return the author with matching ID
     * @throws MissingAuthorException if no author could be found with matching ID
     */
    public GetAuthorById getById(final UUID id) throws MissingAuthorException
    {
        return this.read(() -> this.authors.stream()
            .filter(a -> a.id().equals(id))
            .findFirst()
            .map(GetAuthorById::from)
            .orElseThrow(() -> new MissingAuthorException(id)));
    }

    /**
     * Queries the name index of the author {@link List} for authors with names
     * containing <code>containsNameSearch</code> ignoring case.
     *
     * @param containsNameSearch the contains search text for the query
     * @return a read-only list of all authors matching the query
     */
    public List<SearchAuthorByName> searchByName(final String containsNameSearch)
    {
        return this.read(() -> this.authors.stream()
            .filter(a -> a.name().toLowerCase(Locale.ROOT).contains(containsNameSearch.toLowerCase(Locale.ROOT)))
            .limit(DEFAULT_PAGE_SIZE)
            .map(SearchAuthorByName::from)
            .toList());
    }

    private void validateInsert(final List<InsertAuthor> insert) throws InvalidIsbnException, InvalidGenreException
    {
        final List<InsertAuthorBook> insertBooks = insert.stream()
            .filter(a -> a.books() != null)
            .flatMap(a -> a.books().stream())
            .toList();

        for (final var book : insertBooks)
        {
            // check for isbn uniqueness in the insert and the storage
            final String isbn = book.isbn();
            if (insertBooks.stream().filter(b -> b.isbn().equals(isbn)).count() > 1
                || this.books.stream().anyMatch(b -> b.isbn().equals(isbn)))
            {
                throw new InvalidIsbnException(isbn);
            }

            // check if genres exist
            for (final var genre : book.genres())
            {
                if (!this.genres.contains(genre))
                {
                    throw InvalidGenreException.doesNotExist(genre);
                }
            }
        }
    }
}
