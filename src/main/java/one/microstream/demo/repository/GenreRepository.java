package one.microstream.demo.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.GenericRepository;
import one.microstream.demo.domain.Genre;
import one.microstream.demo.exception.InvalidGenreException;
import one.microstream.demo.exception.MissingGenreException;

import java.util.Set;

/**
 * Repository for finding and modifying genres. All methods hold a cluster-wide
 * read or write lock to ensure consistency with background threads modifying
 * data received from message queues.
 */
@Repository
public interface GenreRepository extends GenericRepository<Genre, Long>
{
    /**
     * Adds the specified genre to the genre set and stores the set.
     *
     * @param genre the genre to insert
     * @throws InvalidGenreException if the specified genre already exists
     * @see Set#add(Object)
     */
    Genre insert(final String name);

    /**
     * Lists all genres contained in the genre set.
     *
     * @return an unmodifiable {@link Set} containing all genres
     */
    Set<String> findAllName();

    /**
     * Removes the specified genre from the genre set and stores the set.
     *
     * @param genre the genre to remove
     * @throws MissingGenreException if the specified genre could not be found
     */
    void deleteOneByName(final String genre);
}
