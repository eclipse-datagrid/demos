package one.microstream.demo.gigamap;

import one.microstream.demo.domain.Book;
import org.eclipse.store.gigamap.types.*;

import java.util.UUID;

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

    public static final IndexerMultiValue<Book, String> GENRES = new IndexerMultiValue.Abstract<Book, String>()
    {
        @Override
        public String name()
        {
            return "genres";
        }

        @Override
        public Iterable<? extends String> indexEntityMultiValue(final Book entity)
        {
            return entity.genres();
        }

        @Override
        public Class<String> keyType()
        {
            return String.class;
        }
    };

    public static final IndexerString<Book> TITLE = new IndexerString.Abstract<>()
    {
        @Override
        public String name()
        {
            return "title";
        }

        @Override
        protected String getString(final Book entity)
        {
            return entity.title();
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
