package one.microstream.demo.lucene;

import one.microstream.demo.Application;
import one.microstream.demo.domain.DataRoot;
import org.apache.lucene.index.IndexFileNames;
import org.apache.lucene.store.*;
import org.eclipse.serializer.reference.Lazy;
import org.eclipse.store.gigamap.lucene.DirectoryCreator;
import org.eclipse.store.storage.types.StorageManager;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

public class EclipseStoreDirectory extends BaseDirectory
{
    private final TempFileNameCreator tempFileName = new TempFileNameCreator.Default();
    private final ConcurrentHashMap<String, FileEntry> files = new ConcurrentHashMap<>();
    private final OutputAsBytes outputToInput;
    private final ByteBuffersDataOutputSupplier bbOutputSupplier;

    public EclipseStoreDirectory()
    {
        this(new TransientSingleInstanceLockFactory());
    }

    public EclipseStoreDirectory(LockFactory lockFactory)
    {
        this(lockFactory, new ByteBuffersDataOutputSupplier.Default(), new OutputAsBytes.ManyBuffers());
    }

    public EclipseStoreDirectory(
        LockFactory factory,
        ByteBuffersDataOutputSupplier bbOutputSupplier,
        OutputAsBytes outputToInput
    )
    {
        super(factory);
        this.outputToInput = Objects.requireNonNull(outputToInput);
        this.bbOutputSupplier = Objects.requireNonNull(bbOutputSupplier);
    }

    @Override
    public String[] listAll() throws IOException
    {
        ensureOpen();
        return files.keySet().stream().sorted().toArray(String[]::new);
    }

    @Override
    public void deleteFile(String name) throws IOException
    {
        ensureOpen();
        FileEntry removed = files.remove(name);
        if (removed == null)
        {
            throw new NoSuchFileException(name);
        }
    }

    @Override
    public long fileLength(String name) throws IOException
    {
        ensureOpen();
        FileEntry file = files.get(name);
        if (file == null)
        {
            throw new NoSuchFileException(name);
        }
        return file.length();
    }

    public boolean fileExists(String name)
    {
        ensureOpen();
        FileEntry file = files.get(name);
        return file != null;
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context) throws IOException
    {
        this.ensureOpen();
        FileEntry e = new FileEntry(name);
        if (files.putIfAbsent(name, e) != null)
        {
            throw new FileAlreadyExistsException("File already exists: " + name);
        }
        return e.createOutput(outputToInput);
    }

    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context)
        throws IOException
    {
        this.ensureOpen();
        while (true)
        {
            String name = IndexFileNames.segmentFileName(prefix, tempFileName.createTempFileName(suffix), "tmp");
            FileEntry e = new FileEntry(name);
            if (files.putIfAbsent(name, e) == null)
            {
                return e.createOutput(outputToInput);
            }
        }
    }

    @Override
    public void rename(String source, String dest) throws IOException
    {
        this.ensureOpen();

        FileEntry file = files.get(source);
        if (file == null)
        {
            throw new NoSuchFileException(source);
        }
        if (files.putIfAbsent(dest, file) != null)
        {
            throw new FileAlreadyExistsException(dest);
        }
        if (!files.remove(source, file))
        {
            throw new IllegalStateException("File was unexpectedly replaced: " + source);
        }
        files.remove(source);
    }

    @Override
    public void sync(Collection<String> names) throws IOException
    {
        this.ensureOpen();
    }

    @Override
    public void syncMetaData() throws IOException
    {
        this.ensureOpen();
    }

    @Override
    public IndexInput openInput(String name, IOContext context) throws IOException
    {
        this.ensureOpen();
        FileEntry e = files.get(name);
        if (e == null)
        {
            throw new NoSuchFileException(name);
        }
        else
        {
            return e.openInput();
        }
    }

    @Override
    public void close() throws IOException
    {
        System.out.println("DEBUG STORING ALL");
        final var s = Application.SM.createEagerStorer();
        s.store(Application.SM.root());
        s.commit();
    }

    @Override
    public Set<String> getPendingDeletions()
    {
        return Collections.emptySet();
    }

    private final class FileEntry
    {
        private final String fileName;

        private volatile IndexInput content;
        private volatile long cachedLength;

        public FileEntry(String name)
        {
            this.fileName = name;
        }

        public long length()
        {
            // We return 0 length until the IndexOutput is closed and flushed.
            return cachedLength;
        }

        public IndexInput openInput() throws IOException
        {
            IndexInput local = this.content;
            if (local == null)
            {
                throw new AccessDeniedException("Can't open a file still open for writing: " + fileName);
            }

            return local.clone();
        }

        IndexOutput createOutput(OutputAsBytes outputToInput) throws IOException
        {
            if (content != null)
            {
                throw new IOException("Can only write to a file once: " + fileName);
            }

            String clazzName = ByteBuffersDirectory.class.getSimpleName();
            String outputName = String.format(Locale.ROOT, "%s output (file=%s)", clazzName, fileName);

            return new ByteBuffersIndexOutput(
                bbOutputSupplier.supplyByteBuffersDataOutput(),
                outputName,
                fileName,
                new CRC32(),
                (output) ->
                {
                    content = outputToInput.outputAsBytes(fileName, output);
                    cachedLength = output.size();
                }
            );
        }
    }

    public static class Creator extends DirectoryCreator
    {
        public Creator(
        )
        {
        }

        @Override
        public Directory createDirectory()
        {
            final StorageManager sm = Application.SM;
            var root = ((Lazy<DataRoot>)sm.root()).get();
            if (root.luceneDirectory == null)
            {
                System.out.println("GENERATING NEW BYTEBUFFERSDIRECTORY");
                root.luceneDirectory = new EclipseStoreDirectory();
                sm.store(root);
            }
            return root.luceneDirectory;
        }
    }
}
