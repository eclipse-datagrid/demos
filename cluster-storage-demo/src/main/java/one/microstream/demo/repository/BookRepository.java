package one.microstream.demo.repository;

import java.util.*;

import io.micronaut.eclipsestore.RootProvider;
import jakarta.inject.Singleton;
import one.microstream.demo.domain.Author;
import one.microstream.demo.domain.Book;
import one.microstream.demo.domain.DataRoot;
import one.microstream.demo.dto.*;
import one.microstream.demo.exception.*;
import org.eclipse.datagrid.cluster.nodelibrary.types.ClusterLockScope;
import org.eclipse.serializer.concurrency.LockedExecutor;
import org.eclipse.store.storage.types.StorageManager;

/**
 * Repository for finding and modifying books. All methods hold a cluster-wide read or write lock to ensure consistency
 * with background threads modifying data received from message queues.
 *
 * <p>
 * Note: All results returned from search queries are limited to {@link BookRepository#DEFAULT_PAGE_SIZE}
 */
@Singleton
public class BookRepository extends ClusterLockScope
{
    private static final int DEFAULT_PAGE_SIZE = 512;

    private final List<Book> books;
    private final List<Author> authors;
    private final Set<String> genres;
    private final StorageManager storageManager;

    public BookRepository(
        final LockedExecutor executor,
        final RootProvider<DataRoot> rootProvider,
        final StorageManager storageManager
    )
    {
        super(executor);
        final var root = rootProvider.root();
        this.books = root.books();
        this.authors = root.authors();
        this.genres = root.genres();
        this.storageManager = storageManager;
    }

    /**
     * Adds the specified books to the books {@link List} and stores it.
     *
     * @param insert the books to add
     * @return a read-only list of the added books
     * @throws InvalidAuthorException if an author could not be found from the specified books
     * @throws InvalidIsbnException   if a duplicate ISBN was found
     * @throws InvalidGenreException  if a genre could not be found from the specified books
     */
    public List<GetBookById> insert(final List<InsertBook> insert)
        throws InvalidAuthorException,
        InvalidIsbnException,
        InvalidGenreException
    {
        final var returnDtos = new ArrayList<GetBookById>(insert.size());

        this.write(() ->
        {
            this.validateInsert(insert);

            // these are the authors that will have to be modified from the insert
            final Map<UUID, Author> cachedAuthors = new HashMap<>();
            for (final var insertBook : insert)
            {
                cachedAuthors.computeIfAbsent(
                    insertBook.authorId(),
                    id -> this.authors.stream()
                        .filter(a -> a.id().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new InvalidAuthorException(id))
                );
            }

            final List<Book> newBooks = insert.stream()
                .map(
                    b -> new Book(
                        UUID.randomUUID(),
                        b.isbn(),
                        b.title(),
                        b.description(),
                        b.pages(),
                        b.genres(),
                        b.publicationDate(),
                        cachedAuthors.get(b.authorId())
                    )
                )
                .toList();

            if (!newBooks.isEmpty())
            {
                this.books.addAll(newBooks);
                this.storageManager.store(this.books);
            }

            for (final var book : newBooks)
            {
                // add the new books to the author book lists
                book.author().books().get().add(book);

                // add as return value
                returnDtos.add(GetBookById.from(book));
            }

            // only store the changed author book lists
            this.storageManager.storeAll(cachedAuthors.values().stream().map(a -> a.books().get()).toList());
        });

        return Collections.unmodifiableList(returnDtos);
    }

    /**
     * Updates the book with the specified values by replacing it and stores the books {@link List}.
     *
     * @param id     the ID of the book to update
     * @param update the new values for the book
     * @throws MissingBookException if no book could be found for the specified ID
     */
    public void update(final UUID id, final UpdateBook update) throws MissingBookException
    {
        this.write(() ->
        {
            int index = -1;
            for (int i = 0; i < this.books.size(); i++)
            {
                if (this.books.get(i).id().equals(id))
                {
                    index = i;
                    break;
                }
            }

            if (index == -1)
            {
                throw new MissingBookException(id);
            }

            final Book storedBook = this.books.get(index);
            final Book newBook = new Book(
                id,
                update.isbn(),
                update.title(),
                update.description(),
                update.pages(),
                update.genres(),
                update.publicationDate(),
                storedBook.author()
            );

            this.books.set(index, newBook);
            this.storageManager.store(this.books);

            // also update author books
            final var authorBooks = this.authors.stream()
                .filter(a -> a.id().equals(storedBook.author().id()))
                .findFirst()
                .get()
                .books()
                .get();
            authorBooks.removeIf(b -> b.id().equals(id));
            authorBooks.add(newBook);
            this.storageManager.store(authorBooks);
        });
    }

    /**
     * Removes the books with the specified IDs from the books {@link List} and stores it.
     *
     * @param ids the IDs of the books to remove
     * @throws MissingBookException if a book with the specified ID could not be found
     */
    public void delete(final Iterable<UUID> ids) throws MissingBookException
    {
        this.write(() ->
        {
            final var cachedBooks = new ArrayList<Book>();
            for (final UUID id : ids)
            {
                // ensure books exist
                cachedBooks.add(this.books.stream()
                    .filter(b -> b.id().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new MissingBookException(id)));
            }
            if (!cachedBooks.isEmpty())
            {
                final var touchedSets = new HashSet<Set<Book>>();
                for (final var book : cachedBooks)
                {
                    // update books gigamap
                    this.books.remove(book);
                    // update author book set
                    final var authorBooks = book.author().books().get();
                    authorBooks.remove(book);
                    touchedSets.add(authorBooks);
                }
                this.storageManager.store(this.books);
                this.storageManager.storeAll(touchedSets);
            }
        });
    }

    /**
     * Returns a book matching the specified ID.
     *
     * @param id the ID of the book to return
     * @return the book with matching ID
     * @throws MissingBookException if the book could not be found
     */
    public GetBookById getById(final UUID id) throws MissingBookException
    {
        return this.read(() -> this.books.stream()
            .filter(b -> b.id().equals(id))
            .findFirst()
            .map(GetBookById::from)
            .orElseThrow(() -> new MissingBookException(id))
        );
    }

    /**
     * Returns a book matching the specified ISBN.
     *
     * @param isbn the ISBN of the book to return
     * @return the book with matching ISBN
     * @throws MissingBookException if the book could not be found
     */
    public GetBookById getByISBN(final String isbn) throws MissingBookException
    {
        return this.read(() -> this.books.stream()
            .filter(b -> b.isbn().equals(isbn))
            .findFirst()
            .map(GetBookById::from)
            .orElseThrow(() -> new MissingBookException(isbn))
        );
    }

    /**
     * Queries the ID index of the author {@link List} for the specified ID and returns a list of all books from the
     * author.
     *
     * @param id the ID of the author
     * @return a read-only list of all books from the author
     * @throws MissingAuthorException if the author could not be found
     */
    public List<SearchBookByAuthor> searchByAuthor(final UUID id) throws MissingAuthorException
    {
        return this.read(() -> this.authors.stream()
            .filter(a -> a.id().equals(id))
            .findFirst()
            .orElseThrow(() -> new MissingAuthorException(id))
            .books()
            .get()
            .stream()
            .limit(DEFAULT_PAGE_SIZE)
            .map(SearchBookByAuthor::from)
            .toList()
        );
    }

    /**
     * Queries the title index of the books {@link List} for the specified
     * <code>titleWildcardSearch</code> with a <code>"title:*search*"</code>
     * wildcard query.
     *
     * @param titleWildcardSearch the wildcard search text the title field will be searched with
     * @return a read-only list of all found books for the specified query
     */
    public List<SearchBookByTitle> searchByTitle(final String titleWildcardSearch)
    {
        final var titleWildcardSearchLowercase = titleWildcardSearch.toLowerCase(Locale.ROOT);
        return this.read(() -> this.books.stream()
            .filter(b -> b.title().toLowerCase(Locale.ROOT).contains(titleWildcardSearchLowercase))
            .map(SearchBookByTitle::from)
            .toList()
        );
    }

    /**
     * Searches the books {@link List} for the specified genres, returning every book which contains all the
     * specified genres.
     *
     * @param genres the genres which will be searched for
     * @return a list of all found books for the specified set of genres
     */
    public List<SearchBookByGenre> searchByGenre(final Set<String> genres)
    {
        return this.read(() -> this.books.stream()
            .filter(b -> b.genres().containsAll(genres))
            .map(SearchBookByGenre::from)
            .toList());
    }

    private void validateInsert(final List<InsertBook> insert) throws InvalidIsbnException, InvalidGenreException
    {
        for (final var book : insert)
        {
            // check for ISBN uniqueness in the insert and the storage
            final String isbn = book.isbn();
            if (insert.stream().map(InsertBook::isbn).filter(isbn::equals).count() > 1
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
