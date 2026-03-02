
package one.microstream.demo.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DataRoot
{
    private final List<Author> authors = new ArrayList<>();
    private final List<Book> books = new ArrayList<>();
    private final Set<String> genres = new HashSet<>();

    public DataRoot()
    {
        super();
    }

    public List<Author> authors()
    {
        return this.authors;
    }

    public List<Book> books()
    {
        return this.books;
    }

    public Set<String> genres()
    {
        return this.genres;
    }
}
