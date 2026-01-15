package one.microstream.demo.lucene;

import org.apache.lucene.store.ByteBuffersDataOutput;

public abstract class ByteBuffersDataOutputSupplier
{
    public abstract ByteBuffersDataOutput supplyByteBuffersDataOutput();

    public static class Default extends ByteBuffersDataOutputSupplier
    {
        @Override
        public ByteBuffersDataOutput supplyByteBuffersDataOutput()
        {
            return new ByteBuffersDataOutput();
        }
    }
}
