package one.microstream.demo.lucene;

import org.apache.lucene.store.*;
import org.eclipse.datagrid.cluster.nodelibrary.types.ClusterLockScope;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.store.gigamap.lucene.DirectoryCreator;
import org.eclipse.store.storage.types.StorageManager;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class EclipseStoreDirectory extends BaseDirectory
{
    private final Map<String, FileEntry> files = new HashMap<>();

    private final StorageManager storageManager;
    private final ClusterLockScope clusterLockScope;

    public EclipseStoreDirectory(
        final LockFactory lockFactory,
        final StorageManager storageManager,
        final ClusterLockScope clusterLockScope
    )
    {
        super(lockFactory);
        this.storageManager = Objects.requireNonNull(storageManager);
        this.clusterLockScope = Objects.requireNonNull(clusterLockScope);
    }

    @Override
    public String[] listAll()
    {
        ensureOpen();
        return clusterLockScope.read(() -> files.keySet().stream().sorted().toArray(String[]::new));
    }

    @Override
    public void deleteFile(final String name) throws IOException
    {
        ensureOpen();
        final var entry = this.files.remove(name);
        if (entry == null)
        {
            throw new NoSuchFileException(name);
        }
    }

    @Override
    public long fileLength(final String name) throws NoSuchFileException
    {
        ensureOpen();
        final var entry = this.files.remove(name);
        if (entry == null)
        {
            throw new NoSuchFileException(name);
        }
        return entry.getLength();
    }

    @Override
    public IndexOutput createOutput(final String fileName, final IOContext context) throws FileAlreadyExistsException
    {
        ensureOpen();
        if (files.get(fileName) != null)
        {
            throw new FileAlreadyExistsException(fileName);
        }
        final var entry = new FileEntry(100_000, Lazy.Reference(new byte[100_000]));
        this.files.put(fileName, entry);
        return new EclipseStoreIndexOutput(fileName, entry);
    }

    @Override
    public IndexOutput createTempOutput(final String prefix, final String suffix, final IOContext context)
        throws IOException
    {
        final String fileName = prefix + UUID.randomUUID().toString() + suffix;
        return createOutput(fileName, context);
    }

    @Override
    public void sync(final Collection<String> names) throws IOException
    {
        final var eagerStore = this.storageManager.createEagerStorer();
        this.files.entrySet()
            .stream()
            .filter(e -> names.contains(e.getKey()))
            .forEach(e -> eagerStore.store(e.getValue()));
        eagerStore.commit();
    }

    @Override
    public void syncMetaData() throws IOException
    {

    }

    @Override
    public void rename(final String source, final String dest) throws IOException
    {

    }

    @Override
    public IndexInput openInput(final String name, final IOContext context) throws IOException
    {
        return null;
    }

    @Override
    public Lock obtainLock(final String name) throws IOException
    {
        return null;
    }

    @Override
    public void close() throws IOException
    {

    }

    @Override
    public Set<String> getPendingDeletions() throws IOException
    {
        return Set.of();
    }

    public static class EclipseStoreDirectoryCreator extends DirectoryCreator
    {
        @Override
        public Directory createDirectory()
        {
            return new EclipseStoreDirectory();
        }
    }
}
