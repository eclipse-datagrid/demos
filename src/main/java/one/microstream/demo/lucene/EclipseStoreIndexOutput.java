package one.microstream.demo.lucene;

import org.apache.lucene.store.IndexOutput;

import java.io.IOException;
import java.util.Locale;
import java.util.zip.CRC32;

public class EclipseStoreIndexOutput extends IndexOutput
{
    private final FileEntry entry;

    private final CRC32 checksum = new CRC32();
    private long pointer = 0;

    public EclipseStoreIndexOutput(final String fileName, FileEntry fileEntry)
    {
        super(
            String.format(Locale.ROOT, "%s(file=%s)", EclipseStoreIndexOutput.class.getSimpleName(), fileName),
            fileName
        );
        this.entry = fileEntry;
    }

    @Override
    public void close()
    {
    }

    @Override
    public long getFilePointer()
    {
        return pointer;
    }

    @Override
    public long getChecksum() throws IOException
    {
        return checksum.getValue();
    }

    @Override
    public void writeByte(final byte b) throws IOException
    {
        final byte[] content = this.entry.getContent().get();
        if (this.pointer == content.length)
        {
            throw new IOException("Files bigger than 100KB are not supported");
        }
        this.checksum.update((int)b);
        content[(int)this.pointer] = b;
        this.pointer++;
        this.entry.setLength(this.pointer);
    }

    @Override
    public void writeBytes(final byte[] b, final int offset, final int length) throws IOException
    {
        final byte[] content = this.entry.getContent().get();
        if (this.pointer + length >= content.length)
        {
            throw new IOException("Files bigger than 100KB are not supported");
        }
        this.checksum.update(b, offset, length);
        System.arraycopy(b, offset, content, (int)pointer, length);
        pointer += length;
        this.entry.setLength(pointer);
    }
}
