package one.microstream.demo.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.NaturalId;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Serdeable
@Introspected
@Entity
public class Book
{
    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NaturalId
    @NonNull
    @NotBlank
    @Column(nullable = false, unique = true)
    private String isbn;
    @NonNull
    @NotBlank
    @Column(nullable = false)
    private String title;
    @NonNull
    @NotBlank
    @Column(nullable = false, length = 1023)
    private String description;
    @Positive
    @Column(nullable = false)
    private int pages;
    @NonNull
    @Column(nullable = false)
    private LocalDate publicationDate;
    @NonNull
    @Valid
    @ManyToOne(optional = false)
    private Author author;
    @NonNull
    @ManyToMany
    private Set<@Valid Genre> genres = new HashSet<>();

    public Book()
    {
    }

    public @NonNull UUID getId()
    {
        return id;
    }

    public void setId(@NonNull final UUID id)
    {
        this.id = id;
    }

    public @NonNull @NotBlank String getIsbn()
    {
        return isbn;
    }

    public void setIsbn(@NonNull @NotBlank final String isbn)
    {
        this.isbn = isbn;
    }

    public @NonNull @NotBlank String getTitle()
    {
        return title;
    }

    public void setTitle(@NonNull @NotBlank final String title)
    {
        this.title = title;
    }

    public @NonNull @NotBlank String getDescription()
    {
        return description;
    }

    public void setDescription(@NonNull @NotBlank final String description)
    {
        this.description = description;
    }

    public @Positive int getPages()
    {
        return pages;
    }

    public void setPages(final @Positive int pages)
    {
        this.pages = pages;
    }

    public @NonNull LocalDate getPublicationDate()
    {
        return publicationDate;
    }

    public void setPublicationDate(@NonNull final LocalDate publicationDate)
    {
        this.publicationDate = publicationDate;
    }

    public @NonNull @Valid Author getAuthor()
    {
        return author;
    }

    public void setAuthor(@NonNull @Valid final Author author)
    {
        this.author = author;
    }

    public @NonNull Set<Genre> getGenres()
    {
        return genres;
    }

    public void setGenres(@NonNull final Set<Genre> genres)
    {
        this.genres = genres;
    }
}
