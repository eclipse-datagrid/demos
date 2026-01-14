package one.microstream.demo.bean;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import one.microstream.demo.domain.Book;
import one.microstream.demo.lucene.BookDocumentPopulator;
import one.microstream.demo.lucene.EclipseStoreDirectory.EclipseStoreDirectoryCreator;
import org.eclipse.serializer.concurrency.LockedExecutor;
import org.eclipse.store.gigamap.lucene.LuceneContext;
import org.eclipse.store.storage.types.StorageManager;

@Factory
public class LuceneContextFactory
{
    @Singleton
    public LuceneContext<Book> buildBookLuceneContext(
        @Property(name = "app.lucene.index.book.storage-directory") final String storageDirectory,
        StorageManager storageManager,
        LockedExecutor lockedExecutor
    )
    {
        return LuceneContext.New(
            new EclipseStoreDirectoryCreator(storageManager, lockedExecutor),
            new BookDocumentPopulator()
        );
    }
}
