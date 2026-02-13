package one.microstream.demo.repository;

import java.util.List;
import java.util.UUID;

import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.QueryHint;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;
import jakarta.validation.constraints.NotBlank;
import one.microstream.demo.domain.Author;
import org.hibernate.jpa.HibernateHints;

/**
 * Repository for finding and modifying authors. All methods hold a cluster-wide
 * read or write lock to ensure consistency with background threads modifying
 * data received from message queues.
 *
 * <p>
 * Note: All results returned from search queries are limited to
 * {@link AuthorRepository#DEFAULT_PAGE_SIZE}
 */
@Repository
public interface AuthorRepository extends PageableRepository<Author, UUID>
{
    /**
     * Updates the author with the specified values by replacing it and stores the
     * authors {@link GigaMap}.
     *
     * @param id     the ID of the author to update
     * @param update the new values for the author
     * @throws MissingAuthorException if no author could be found for the specified
     *                                ID
     */
    @QueryHint(name = "jakarta.persistence.FlushModeType", value = "AUTO")
    void updateAuthorInfo(
        @Id UUID id,
        @Parameter("name") @NonNull @NotBlank String name,
        @Parameter("about") @NonNull @NotBlank String about
    );

    /**
     * Removes the books with the specified IDs from the books {@link GigaMap} and
     * stores it.
     *
     * @param ids the IDs of the books to remove
     * @throws MissingAuthorException if an author could not be found for one of the
     *                                specified IDs
     */
    void deleteAllById(Iterable<UUID> id);

    /**
     * Queries the name index of the author {@link GigaMap} for authors with names
     * containing <code>containsNameSearch</code> ignoring case.
     *
     * @param containsNameSearch the contains search text for the query
     * @return a read-only list of all authors matching the query
     */
    @QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true")
    List<Author> searchByNameIlike(String nameIlikeQueryString);
}
