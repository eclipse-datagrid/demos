package one.microstream.demo;

import net.datafaker.Faker;
import one.microstream.demo.dto.GenerateData.DataGenerationConfig;
import one.microstream.demo.dto.GetAuthorById;
import one.microstream.demo.dto.InsertAuthor;
import one.microstream.demo.dto.InsertBook;
import one.microstream.demo.exception.InvalidGenreException;
import one.microstream.demo.exception.MissingBookException;
import one.microstream.demo.repository.AuthorRepository;
import one.microstream.demo.repository.BookRepository;
import one.microstream.demo.repository.GenreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

public class DataGenerator
{
    private static final Logger LOG = LoggerFactory.getLogger(DataGenerator.class);

    private final GenreRepository genres;
    private final AuthorRepository authors;
    private final BookRepository books;
    private final DataGenerationConfig genreConf;
    private final DataGenerationConfig authorConf;
    private final DataGenerationConfig bookConf;

    public DataGenerator(
        GenreRepository genres,
        AuthorRepository authors,
        BookRepository books,
        DataGenerationConfig genreConf,
        DataGenerationConfig authorConf,
        DataGenerationConfig bookConf
    )
    {
        this.genres = genres;
        this.authors = authors;
        this.books = books;
        this.genreConf = genreConf;
        this.authorConf = authorConf;
        this.bookConf = bookConf;
    }

    public void generateData()
    {
        this.generateGenres();
        final var authorIds = this.generateAuthors();
        this.generateBooks(authorIds);
    }

    /**
     * Tries to generate the specified amount of genres. Duplicates will be ignored, this means the resulting set might
     * contain fewer genres than specified.
     */
    private void generateGenres()
    {
        final var seed = genreConf.seed();
        LOG.info("Generating {} genres with seed {}", genreConf.count(), seed);
        final var faker = createFaker(seed);
        for (int i = 0; i < genreConf.count(); i++)
        {
            try
            {
                int tryCount = 0;
                for (; tryCount < 10; tryCount++)
                {
                    try
                    {
                        genres.insert(faker.book().genre());
                        break;
                    }
                    catch (final InvalidGenreException ignored)
                    {
                        // genre already exists, retry...
                    }
                }
                if (tryCount == 10)
                {
                    LOG.error("Failed to generate more genres at iteration {}", i);
                    break;
                }
            }
            catch (InvalidGenreException e)
            {
                // ignore duplicates
            }
        }
    }

    private List<UUID> generateAuthors()
    {
        final var seed = authorConf.seed();
        LOG.info("Generating {} authors with seed {}", this.authorConf.count(), seed);
        final var faker = createFaker(seed);
        final var authors = new ArrayList<InsertAuthor>(authorConf.count());
        for (int i = 0; i < authorConf.count(); i++)
        {
            authors.add(new InsertAuthor(faker.book().author(), faker.company().catchPhrase(), null));
        }
        final var insertedAuthors = this.authors.insert(authors);
        return insertedAuthors.stream().map(GetAuthorById::id).toList();
    }

    private void generateBooks(final List<UUID> authorIds)
    {
        final var seed = bookConf.seed();
        LOG.info("Generating {} books with seed {}", bookConf.count(), seed);
        final var availableGenres = genres.list();

        final var faker = createFaker(seed);
        final var newBooks = new ArrayList<InsertBook>(bookConf.count());
        for (int i = 0; i < bookConf.count(); i++)
        {
            final var randomGenres = new ArrayList<>(availableGenres);
            final var genreCount = faker.number().numberBetween(1, availableGenres.size());
            // start with all available genres and remove random entries until we have reached the target amount
            while (randomGenres.size() > genreCount)
            {
                randomGenres.remove(faker.number().numberBetween(0, randomGenres.size()));
            }

            int tryCount = 0;
            for (; tryCount < 10; tryCount++)
            {
                final var newBook = new InsertBook(
                    faker.code().isbn10(),
                    faker.book().title(),
                    faker.lorem().sentence(faker.number().numberBetween(50, 100)),
                    faker.number().numberBetween(1, 1000),
                    new HashSet<>(randomGenres),
                    randomDateBetween(faker, LocalDate.of(1900, 1, 1), LocalDate.now().minusDays(1)),
                    authorIds.get(faker.number().numberBetween(0, authorIds.size()))
                );
                try
                {
                    // we expect this to throw as there should not be a book with identical ISBN
                    this.books.getByISBN(newBook.isbn());
                }
                catch (final MissingBookException ignored)
                {
                    newBooks.add(newBook);
                    break;
                }
            }
            if (tryCount == 10)
            {
                LOG.error("Failed to generate more books at iteration {}", i);
                break;
            }
        }
        this.books.insert(newBooks);
    }

    private static Faker createFaker(Long seed)
    {
        if (seed == null)
        {
            return new Faker();
        }
        else
        {
            return new Faker(new Random(seed));
        }
    }

    private static LocalDate randomDateBetween(Faker faker, LocalDate start, LocalDate end)
    {
        final var zone = ZoneOffset.UTC;
        final var convertedStart = Instant.from(start.atTime(LocalTime.of(0, 0)).atOffset(zone));
        final var convertedEnd = Instant.from(end.atTime(LocalTime.of(0, 0)).atOffset(zone));
        final Instant between = faker.timeAndDate().between(convertedStart, convertedEnd);
        return LocalDate.ofInstant(between, zone);
    }
}
