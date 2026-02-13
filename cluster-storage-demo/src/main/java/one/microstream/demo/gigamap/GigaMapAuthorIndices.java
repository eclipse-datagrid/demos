package one.microstream.demo.gigamap;

import java.util.UUID;

import org.eclipse.store.gigamap.types.BinaryIndexerUUID;
import org.eclipse.store.gigamap.types.IndexerString;

import one.microstream.demo.domain.Author;

public final class GigaMapAuthorIndices
{
    public static final BinaryIndexerUUID<Author> ID = new BinaryIndexerUUID.Abstract<>()
    {
        @Override
        public String name()
        {
            return "id";
        }

        @Override
        protected UUID getUUID(final Author entity)
        {
            return entity.id();
        }
    };

    public static final IndexerString<Author> NAME = new IndexerString.Abstract<>()
    {
        @Override
        public String name()
        {
            return "name";
        }

        @Override
        protected String getString(final Author entity)
        {
            return entity.name();
        }
    };

    private GigaMapAuthorIndices()
    {
    }
}
