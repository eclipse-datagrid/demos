package one.microstream.demo.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Serdeable
@Introspected
@Entity
public class Author
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NonNull
    @NotBlank
    @Column(nullable = false)
    private String name;
    @NonNull
    @NotBlank
    @Column(nullable = false)
    private String about;
    @OneToMany
    private Set<@Valid Book> books = new HashSet<>();

    public Author()
    {
    }

    public Author(final String name, final String about)
    {
        this.name = name;
        this.about = about;
    }

    public UUID getId()
    {
        return id;
    }

    public void setId(final UUID id)
    {
        this.id = id;
    }

    public @NonNull @NotBlank String getName()
    {
        return name;
    }

    public void setName(@NonNull @NotBlank final String name)
    {
        this.name = name;
    }

    public @NonNull String getAbout()
    {
        return about;
    }

    public void setAbout(@NonNull final String about)
    {
        this.about = about;
    }

    public Set<@Valid Book> getBooks()
    {
        return books;
    }

    public void setBooks(Set<@Valid Book> books)
    {
        this.books = books;
    }
}
