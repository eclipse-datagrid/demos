package one.microstream.demo.bean;

import com.github.javafaker.Faker;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import one.microstream.demo.dto.GetAuthorById;
import one.microstream.demo.dto.InsertAuthor;
import one.microstream.demo.dto.InsertBook;
import one.microstream.demo.exception.InvalidGenreException;
import one.microstream.demo.repository.AuthorRepository;
import one.microstream.demo.repository.BookRepository;
import one.microstream.demo.repository.GenreRepository;
import org.eclipse.datagrid.cluster.nodelibrary.types.ClusterFoundation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

@Singleton
@Requires(property = "app.data.generation.enabled", value = "true")
public class DataGenerator
{
    private static final Logger LOG = LoggerFactory.getLogger(DataGenerator.class);

    private final ClusterFoundation<?> foundation;

    private final GenreRepository genres;
    private final AuthorRepository authors;
    private final BookRepository books;
    private final DataGenerationConfig genreConf;
    private final DataGenerationConfig authorConf;
    private final DataGenerationConfig bookConf;

    public DataGenerator(
        ClusterFoundation<?> foundation,
        GenreRepository genres,
        AuthorRepository authors,
        BookRepository books,
        @Named("genre") DataGenerationConfig genreConf,
        @Named("author") DataGenerationConfig authorConf,
        @Named("book") DataGenerationConfig bookConf
    )
    {
        this.foundation = foundation;
        this.genres = genres;
        this.authors = authors;
        this.books = books;
        this.genreConf = genreConf;
        this.authorConf = authorConf;
        this.bookConf = bookConf;
    }

    @EventListener
    public void generateDataAtStartup(StartupEvent event)
    {
        // don't generate if we are in a cluster as only the writer is allowed
        // to modify the storage, so this has to be called manually in that case
        final var props = foundation.getNodelibraryPropertiesProvider();
        if (!props.isProdMode())
        {
            this.generateData();
        }
    }

    public void generateData()
    {
        generateGenres();
        final var authorIds = generateAuthors();
        generateBooks(authorIds);
    }

    /**
     * Tries to generate the specified amount of genres. Duplicates will be ignored, this means the resulting set might
     * contain fewer genres than specified.
     */
    private void generateGenres()
    {
        final var seed = genreConf.getSeed();
        LOG.info("Generating {} genres with seed {}", genreConf.getCount(), seed);
        final var faker = createFaker(seed);
        for (int i = 0; i < genreConf.getCount(); i++)
        {
            try
            {
                genres.insert(faker.book().genre());
            }
            catch (InvalidGenreException e)
            {
                // ignore duplicates
            }
        }
    }

    private List<UUID> generateAuthors()
    {
        final var seed = authorConf.getSeed();
        LOG.info("Generating {} authors with seed {}", this.authorConf.getCount(), seed);
        final var faker = createFaker(seed);
        final var authors = new ArrayList<InsertAuthor>(authorConf.getCount());
        for (int i = 0; i < authorConf.getCount(); i++)
        {
            authors.add(new InsertAuthor(faker.book().author(), faker.company().catchPhrase(), null));
        }
        final var insertedAuthors = this.authors.insert(authors);
        return insertedAuthors.stream().map(GetAuthorById::id).toList();
    }

    private void generateBooks(final List<UUID> authorIds)
    {
        final var seed = bookConf.getSeed();
        LOG.info("Generating {} books with seed {}", bookConf.getCount(), seed);
        final var availableGenres = genres.list();

        final var faker = createFaker(seed);
        final var books = new ArrayList<InsertBook>(bookConf.getCount());
        for (int i = 0; i < bookConf.getCount(); i++)
        {
            final var randomGenres = new ArrayList<>(availableGenres);
            final var genreCount = faker.number().numberBetween(1, availableGenres.size());
            // start with all available genres and remove random entries until we have reached the target amount
            while (randomGenres.size() > genreCount)
            {
                randomGenres.remove(faker.number().numberBetween(0, randomGenres.size()));
            }

            books.add(new InsertBook(
                faker.code().isbn10(),
                faker.book().title(),
                faker.lorem().sentence(faker.number().numberBetween(50, 100)),
                faker.number().numberBetween(1, 1000),
                new HashSet<>(randomGenres),
                randomDateBetween(faker, LocalDate.of(1900, 1, 1), LocalDate.now().minusDays(1)),
                authorIds.get(faker.number().numberBetween(0, authorIds.size()))
            ));
        }
        this.books.insert(books);
    }

    private static Faker createFaker(Optional<Long> seed)
    {
        return seed.map(s -> Faker.instance(new Random(s))).orElseGet(Faker::instance);
    }

    private static LocalDate randomDateBetween(Faker faker, LocalDate start, LocalDate end)
    {
        final var zone = ZoneOffset.UTC;
        final var convertedStart = Date.from(Instant.from(start.atTime(LocalTime.of(0, 0)).atOffset(zone)));
        final var convertedEnd = Date.from(Instant.from(end.atTime(LocalTime.of(0, 0)).atOffset(zone)));

        final var date = faker.date().between(convertedStart, convertedEnd);
        return LocalDate.ofInstant(date.toInstant(), zone);
    }
}
