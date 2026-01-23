package one.microstream.demo.domain;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;
import java.util.UUID;

@Introspected
@Entity
public class Genre
{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NonNull
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    public Genre()
    {
    }

    public Genre(final UUID id)
    {
        this.id = id;
    }

    public Genre(@NonNull @NotBlank final String name)
    {
        this.name = name;
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

    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof final Genre genre))
            return false;
        return Objects.equals(id, genre.id) && Objects.equals(name, genre.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name);
    }

    @Override
    public String toString()
    {
        return "Genre{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
    }
}
