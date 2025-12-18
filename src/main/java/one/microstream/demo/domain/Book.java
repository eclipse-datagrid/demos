package one.microstream.demo.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * @param id              the unique identifier
 * @param isbn            the isbn identifier
 * @param title           the title of the book
 * @param description     the description of the book which can usually be found on the back
 * @param pages           how many pages the book has
 * @param genres          the genres of the book
 * @param publicationDate when the book was published
 * @param author          the author of the book
 */
public record Book(
    UUID id,
    String isbn,
    String title,
    String description,
    int pages,
    Set<String> genres,
    LocalDate publicationDate,
    Author author
)
{
    @Override
    public int hashCode()
    {
        // use author id to avoid stack overflows
        return Objects.hash(
            this.author.id(),
            this.description,
            this.genres,
            this.id,
            this.isbn,
            this.pages,
            this.publicationDate,
            this.title
        );
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof Book other))
        {
            return false;
        }
        // use author id to avoid stack overflows
        boolean authorsEqual = this.author == null
            && other.author == null
            || this.author != null
            && other.author != null
            && this.author.id().equals(other.author.id());
        return authorsEqual
            && Objects.equals(this.description, other.description)
            && Objects.equals(this.genres, other.genres)
            && Objects.equals(this.id, other.id)
            && Objects.equals(this.isbn, other.isbn)
            && this.pages == other.pages
            && Objects.equals(this.publicationDate, other.publicationDate)
            && Objects.equals(this.title, other.title);
    }

    @Override
    public String toString()
    {
        // use author id to avoid stack overflows
        final var builder = new StringBuilder();
        builder.append("Book [id=")
            .append(this.id)
            .append(", isbn=")
            .append(this.isbn)
            .append(", title=")
            .append(this.title)
            .append(", description=")
            .append(this.description)
            .append(", pages=")
            .append(this.pages)
            .append(", genres=")
            .append(this.genres)
            .append(", publicationDate=")
            .append(this.publicationDate)
            .append(", author=")
            .append(this.author.id())
            .append("]");
        return builder.toString();
    }
}
