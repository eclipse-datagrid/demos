package one.microstream.demo;

import io.micronaut.data.exceptions.EmptyResultException;
import net.datafaker.Faker;
import one.microstream.demo.domain.Author;
import one.microstream.demo.domain.Book;
import one.microstream.demo.dto.GenerateData;
import one.microstream.demo.repository.AuthorRepository;
import one.microstream.demo.repository.BookRepository;
import one.microstream.demo.repository.GenreRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class DataGenerator
{
    private static final Logger LOG = LoggerFactory.getLogger(DataGenerator.class);

    private final GenreRepository genres;
    private final AuthorRepository authors;
    private final BookRepository books;
    private final GenerateData.DataGenerationConfig genreConf;
    private final GenerateData.DataGenerationConfig authorConf;
    private final GenerateData.DataGenerationConfig bookConf;

    public DataGenerator(
        GenreRepository genres,
        AuthorRepository authors,
        BookRepository books,
        GenerateData.DataGenerationConfig genreConf,
        GenerateData.DataGenerationConfig authorConf,
        GenerateData.DataGenerationConfig bookConf
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
        final var authors = this.generateAuthors();
        this.generateBooks(authors);
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
                    catch (final ConstraintViolationException ignored)
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
            catch (final ConstraintViolationException ignored)
            {
                // ignore duplicates
            }
        }
    }

    private List<Author> generateAuthors()
    {
        final var seed = authorConf.seed();
        LOG.info("Generating {} authors with seed {}", this.authorConf.count(), seed);
        final var faker = createFaker(seed);
        final var authors = new ArrayList<Author>(authorConf.count());
        for (int i = 0; i < authorConf.count(); i++)
        {
            final var a = new Author();
            a.setName(faker.book().author());
            a.setAbout(faker.company().catchPhrase());
            authors.add(a);
        }
        return this.authors.saveAll(authors);
    }

    private void generateBooks(final List<Author> authors)
    {
        final var seed = bookConf.seed();
        LOG.info("Generating {} books with seed {}", bookConf.count(), seed);
        final var availableGenres = genres.findAll();

        final var faker = createFaker(seed);
        final var newBooks = new ArrayList<Book>(bookConf.count());
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
                final var newBook = new Book();
                newBook.setIsbn(faker.code().isbn10());
                if (newBooks.stream().anyMatch(b -> b.getIsbn().equals(newBook.getIsbn())))
                {
                    continue;
                }
                newBook.setTitle(faker.book().title());
                newBook.setDescription(faker.lorem().sentence(faker.number().numberBetween(50, 100)));
                newBook.setPages(faker.number().numberBetween(1, 1000));
                newBook.setGenres(new HashSet<>(randomGenres));
                newBook.setPublicationDate(randomDateBetween(
                    faker,
                    LocalDate.of(1900, 1, 1),
                    LocalDate.now().minusDays(1)
                ));
                newBook.setAuthor(authors.get(faker.number().numberBetween(0, authors.size())));
                try
                {
                    // we expect this to throw as there should not be a book with identical ISBN
                    if (this.books.getByIsbn(newBook.getIsbn()) == null)
                    {
                        newBooks.add(newBook);
                        break;
                    }
                }
                catch (final EmptyResultException ignored)
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
        this.books.saveAll(newBooks);
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
