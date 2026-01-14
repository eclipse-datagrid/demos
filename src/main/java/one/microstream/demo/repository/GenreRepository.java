package one.microstream.demo.repository;

import java.util.Collections;
import java.util.Set;

import org.eclipse.datagrid.cluster.nodelibrary.types.ClusterLockScope;
import org.eclipse.serializer.concurrency.LockedExecutor;
import org.eclipse.store.storage.types.StorageManager;

import io.micronaut.eclipsestore.RootProvider;
import jakarta.inject.Singleton;
import one.microstream.demo.domain.DataRoot;
import one.microstream.demo.exception.InvalidGenreException;
import one.microstream.demo.exception.MissingGenreException;

/**
 * Repository for finding and modifying genres. All methods hold a cluster-wide
 * read or write lock to ensure consistency with background threads modifying
 * data received from message queues.
 */
@Singleton
public class GenreRepository extends ClusterLockScope
{
    private final Set<String> genres;
    private final StorageManager storageManager;

    public GenreRepository(
        final LockedExecutor executor,
        final RootProvider<DataRoot> rootProvider,
        final StorageManager storageManager
    )
    {
        super(executor);
        this.genres = rootProvider.root().genres();
        this.storageManager = storageManager;
    }

    /**
     * Adds the specified genre to the genre set and stores the set.
     * 
     * @param genre the genre to insert
     * @throws InvalidGenreException if the specified genre already exists
     * @see Set#add(Object)
     */
    public void insert(final String genre) throws InvalidGenreException
    {
        this.write(() ->
        {
            final boolean modified = this.genres.add(genre);
            if (!modified)
            {
                throw new InvalidGenreException("Genre '%s' already exists.".formatted(genre));
            }
            this.storageManager.store(this.genres);
        });
    }

    /**
     * Lists all genres contained in the genre set.
     * 
     * @return an unmodifiable {@link Set} containing all genres
     */
    public Set<String> list()
    {
        return this.read(() -> Collections.unmodifiableSet(this.genres));
    }

    /**
     * Removes the specified genre from the genre set and stores the set.
     * 
     * @param genre the genre to remove
     * @throws MissingGenreException if the specified genre could not be found
     */
    public void delete(final String genre) throws MissingGenreException
    {
        this.write(() ->
        {
            final boolean modified = this.genres.remove(genre);
            if (!modified)
            {
                throw new MissingGenreException(genre);
            }
            this.storageManager.store(this.genres);
        });
    }
}
