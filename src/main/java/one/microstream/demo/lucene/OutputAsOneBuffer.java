package one.microstream.demo.lucene;

import org.apache.lucene.store.ByteBuffersDataInput;
import org.apache.lucene.store.ByteBuffersDataOutput;
import org.apache.lucene.store.ByteBuffersIndexInput;
import org.apache.lucene.store.IndexInput;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;

public interface OutputAsOneBuffer
{
    IndexInput outputAsOneBuffer(String fileName, ByteBuffersDataOutput output);

    class Default implements OutputAsOneBuffer
    {
        @Override
        public IndexInput outputAsOneBuffer(final String fileName, final ByteBuffersDataOutput output)
        {
            ByteBuffersDataInput dataInput =
                new ByteBuffersDataInput(
                    List.of(
                        ByteBuffer.wrap(output.toArrayCopy()).order(ByteOrder.LITTLE_ENDIAN)));
            String inputName =
                String.format(
                    Locale.ROOT,
                    "%s (file=%s, buffers=%s)",
                    ByteBuffersIndexInput.class.getSimpleName(),
                    fileName,
                    dataInput
                );
            return new ByteBuffersIndexInput(dataInput, inputName);
        }
    }
}
