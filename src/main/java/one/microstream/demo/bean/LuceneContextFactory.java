package one.microstream.demo.bean;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import one.microstream.demo.domain.Book;
import one.microstream.demo.lucene.BookDocumentPopulator;
import org.eclipse.store.gigamap.lucene.DirectoryCreator;
import org.eclipse.store.gigamap.lucene.LuceneContext;

@Factory
public class LuceneContextFactory
{
    @Singleton
    public LuceneContext<Book> buildBookLuceneContext(
        @Property(name = "app.lucene.index.book.storage-directory") final String storageDirectory
    )
    {
        return LuceneContext.New(DirectoryCreator.ByteBuffers(), new BookDocumentPopulator());
    }
}
