package one.microstream.demo.lucene;

import org.apache.lucene.store.IndexInput;

import java.io.EOFException;
import java.io.IOException;

public class EclipseStoreIndexInput extends IndexInput
{
    private final FileEntry entry;
    private final long offset;
    private final long length;

    private long pointer;

    public EclipseStoreIndexInput(final String resourceDescription, final FileEntry entry)
    {
        this(resourceDescription, entry, 0, entry.getLength());
    }

    public EclipseStoreIndexInput(
        final String resourceDescription,
        final FileEntry entry,
        final long offset,
        final long length
    )
    {
        super(resourceDescription);
        this.entry = entry;
        this.offset = offset;
        this.length = length;
        this.pointer = this.offset;
    }

    @Override
    public void close()
    {
    }

    @Override
    public long getFilePointer()
    {
        return this.pointer - this.offset;
    }

    @Override
    public void seek(final long pos) throws IOException
    {
        if (pos >= this.length)
        {
            throw new EOFException(String.format(
                "Tried to seek to offset %d in file with length %d",
                pos,
                this.length
            ));
        }
        this.pointer = pos + offset;
    }

    @Override
    public long length()
    {
        return this.length;
    }

    @Override
    public IndexInput slice(final String sliceDescription, final long offset, final long length)
    {
        final var description = this.getFullSliceDescription(String.format("[offset=%d,length=%d]", offset, length));
        return new EclipseStoreIndexInput(description, this.entry, this.offset + offset, length);
    }

    @Override
    public byte readByte()
    {
        final byte b = this.entry.getContent().get()[(int)this.pointer];
        this.pointer++;
        return b;
    }

    @Override
    public void readBytes(final byte[] dest, final int destOffset, final int len)
    {
        final byte[] fileBuffer = this.entry.getContent().get();
        System.arraycopy(fileBuffer, (int)this.pointer, dest, destOffset, len);
        this.pointer += len;
    }
}
