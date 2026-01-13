package one.microstream.demo.lucene;

import org.eclipse.serializer.reference.Lazy;

public class FileEntry
{
    private long length;
    private Lazy<byte[]> content;

    public FileEntry(final long length, final Lazy<byte[]> content)
    {
        this.length = length;
        this.content = content;
    }

    public long getLength()
    {
        return length;
    }

    public void setLength(final long length)
    {
        this.length = length;
    }

    public Lazy<byte[]> getContent()
    {
        return content;
    }

    public void setContent(final Lazy<byte[]> content)
    {
        this.content = content;
    }
}
