package one.microstream.demo.lucene;

import io.micronaut.context.annotation.Factory;
import io.micronaut.eclipsestore.RootProvider;
import jakarta.inject.Singleton;
import one.microstream.demo.domain.DataRoot;
import org.eclipse.store.gigamap.lucene.LuceneContext;
import org.eclipse.store.gigamap.lucene.LuceneIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
public class LuceneIndexFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(LuceneIndexFactory.class);

    @Singleton
    public LuceneIndex<?> bookIndex(final RootProvider<DataRoot> rootProvider)
    {
        final var gigaIndices = rootProvider.root().books().index();
        @SuppressWarnings("unchecked")
        var bookIndex = gigaIndices.get(LuceneIndex.class);
        if (bookIndex == null)
        {
            LOG.info("Registering new book lucene index.");
            final var context = LuceneContext.New(new BookDocumentPopulator());
            bookIndex = gigaIndices.register(LuceneIndex.Category(context));
        }
        return bookIndex;
    }
}
