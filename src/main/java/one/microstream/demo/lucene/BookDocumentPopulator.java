package one.microstream.demo.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.eclipse.store.gigamap.lucene.DocumentPopulator;

import one.microstream.demo.domain.Book;

public final class BookDocumentPopulator extends DocumentPopulator<Book>
{
    public static final String TITLE_FIELD = "title";
    public static final String GENRES_FIELD = "genres";

    @Override
    public void populate(final Document document, final Book entity)
    {
        document.add(createTextField(TITLE_FIELD, entity.title()));
        for (final var genre : entity.genres())
        {
            document.add(new TextField(GENRES_FIELD, genre, Field.Store.YES));
        }
    }
}
