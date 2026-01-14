package one.microstream.demo.lucene;

import one.microstream.demo.domain.DataRoot;
import org.apache.lucene.store.*;
import org.eclipse.serializer.concurrency.LockedExecutor;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.store.gigamap.lucene.DirectoryCreator;
import org.eclipse.store.storage.types.StorageManager;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EclipseStoreDirectory extends BaseDirectory
{
    private final ConcurrentHashMap<String, FileEntry> files = new ConcurrentHashMap<>();

    private transient final StorageManager storageManager;
    private transient final LockedExecutor lockedExecutor;

    public EclipseStoreDirectory(
        final StorageManager storageManager,
        final LockedExecutor lockedExecutor
    )
    {
        super(new SingleInstanceLockFactory());
        this.storageManager = Objects.requireNonNull(storageManager);
        this.lockedExecutor = Objects.requireNonNull(lockedExecutor);
    }

    @Override
    public String[] listAll() throws AlreadyClosedException
    {
        ensureOpen();
        return lockedExecutor.read(() -> files.keySet().stream().sorted().toArray(String[]::new));
    }

    @Override
    public void deleteFile(final String name) throws NoSuchFileException, AlreadyClosedException
    {
        ensureOpen();
        final var entry = this.files.remove(name);
        if (entry == null)
        {
            throw new NoSuchFileException(name);
        }
        //FIXME this.storageManager.store(this.files);
    }

    @Override
    public long fileLength(final String name) throws NoSuchFileException, AlreadyClosedException
    {
        ensureOpen();
        final var entry = this.files.get(name);
        if (entry == null)
        {
            throw new NoSuchFileException(name);
        }
        return entry.getLength();
    }

    @Override
    public IndexOutput createOutput(final String fileName, final IOContext context)
        throws FileAlreadyExistsException, AlreadyClosedException
    {
        ensureOpen();
        if (files.get(fileName) != null)
        {
            throw new FileAlreadyExistsException(fileName);
        }
        final var entry = new FileEntry(100_000, Lazy.Reference(new byte[100_000]));
        this.files.put(fileName, entry);
        //FIXME this.storageManager.store(this.files);
        return new EclipseStoreIndexOutput(fileName, entry);
    }

    @Override
    public IndexOutput createTempOutput(final String prefix, final String suffix, final IOContext context)
        throws FileAlreadyExistsException, AlreadyClosedException
    {
        final String fileName = prefix + UUID.randomUUID() + suffix + ".tmp";
        return createOutput(fileName, context);
    }

    @Override
    public void sync(final Collection<String> names) throws AlreadyClosedException
    {
        this.ensureOpen();
        final var eagerStore = this.storageManager.createEagerStorer();
        this.files.entrySet()
            .stream()
            .filter(e -> names.contains(e.getKey()))
            .forEach(e -> eagerStore.store(e.getValue()));
        eagerStore.commit();
    }

    @Override
    public void syncMetaData() throws AlreadyClosedException
    {
        this.ensureOpen();
        final var eagerStore = this.storageManager.createEagerStorer();
        eagerStore.store(this.files);
        eagerStore.commit();
    }

    @Override
    public void rename(final String source, final String dest) throws FileSystemException, AlreadyClosedException
    {
        this.ensureOpen();
        final var entry = this.files.get(source);
        if (entry == null)
        {
            throw new NoSuchFileException(source);
        }
        if (this.files.get(dest) != null)
        {
            throw new FileAlreadyExistsException(dest);
        }
        this.files.put(dest, entry);
        this.storageManager.store(this.files);
    }

    @Override
    public IndexInput openInput(final String fileName, final IOContext context)
        throws NoSuchFileException, AlreadyClosedException
    {
        this.ensureOpen();
        final var entry = this.files.get(fileName);
        if (entry == null)
        {
            throw new NoSuchFileException(fileName);
        }
        final var description =
            String.format(Locale.ROOT, "%s(file=%s)", EclipseStoreIndexInput.class.getSimpleName(), fileName);
        return new EclipseStoreIndexInput(description, entry);
    }

    @Override
    public void close()
    {
        this.isOpen = false;
    }

    @Override
    public Set<String> getPendingDeletions()
    {
        return Set.of();
    }

    public static class EclipseStoreDirectoryCreator extends DirectoryCreator
    {
        private final transient StorageManager storageManager;
        private final transient LockedExecutor lockedExecutor;

        public EclipseStoreDirectoryCreator(
            final StorageManager storageManager,
            final LockedExecutor lockedExecutor
        )
        {
            this.storageManager = storageManager;
            this.lockedExecutor = lockedExecutor;
        }

        @Override
        public Directory createDirectory()
        {
            final var dir = new EclipseStoreDirectory(storageManager, lockedExecutor);
            final var root = ((Lazy<DataRoot>)storageManager.root()).get();
            root.luceneDirectory = dir;
            storageManager.store(root);
            return dir;
        }
    }
}
