package one.microstream.demo.gigamap;

import java.util.UUID;

import org.eclipse.store.gigamap.types.BinaryIndexerString;
import org.eclipse.store.gigamap.types.BinaryIndexerUUID;
import org.eclipse.store.gigamap.types.IndexerLocalDate;

import one.microstream.demo.domain.Book;

public final class GigaMapBookIndices
{
    public static final BinaryIndexerUUID<Book> ID = new BinaryIndexerUUID.Abstract<>()
    {
        @Override
        public String name()
        {
            return "id";
        }

        @Override
        protected UUID getUUID(final Book entity)
        {
            return entity.id();
        }
    };

    public static final BinaryIndexerString<Book> ISBN = new BinaryIndexerString.Abstract<>()
    {
        @Override
        public String name()
        {
            return "isbn";
        }

        protected String getString(final Book entity)
        {
            return entity.isbn();
        }
    };

    public static final IndexerLocalDate<Book> PUBLICATION = new IndexerLocalDate.Abstract<>()
    {
        @Override
        public String name()
        {
            return "publication";
        }

        protected java.time.LocalDate getLocalDate(final Book entity)
        {
            return entity.publicationDate();
        }
    };

    private GigaMapBookIndices()
    {
    }
}
