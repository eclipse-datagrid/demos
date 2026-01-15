package one.microstream.demo.lucene;

import org.apache.lucene.store.ByteBuffersDataInput;
import org.apache.lucene.store.ByteBuffersDataOutput;
import org.apache.lucene.store.ByteBuffersIndexInput;
import org.apache.lucene.store.IndexInput;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;

public abstract class OutputAsBytes
{
    public abstract IndexInput outputAsBytes(String fileName, ByteBuffersDataOutput output);

    public static IndexInput outputAsManyBuffers(String fileName, ByteBuffersDataOutput output)
    {
        ByteBuffersDataInput dataInput = output.toDataInput();
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

    public static IndexInput outputAsOneBuffer(String fileName, ByteBuffersDataOutput output)
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

    public static IndexInput outputAsByteArray(String fileName, ByteBuffersDataOutput output)
    {
        return outputAsOneBuffer(fileName, output);
    }

    public static final class ManyBuffers extends OutputAsBytes
    {
        @Override
        public IndexInput outputAsBytes(final String fileName, final ByteBuffersDataOutput output)
        {
            return OutputAsBytes.outputAsManyBuffers(fileName, output);
        }
    }

    public static final class OneBuffer extends OutputAsBytes
    {
        @Override
        public IndexInput outputAsBytes(final String fileName, final ByteBuffersDataOutput output)
        {
            return OutputAsBytes.outputAsOneBuffer(fileName, output);
        }
    }

    public static final class ByteArray extends OutputAsBytes
    {
        @Override
        public IndexInput outputAsBytes(final String fileName, final ByteBuffersDataOutput output)
        {
            return OutputAsBytes.outputAsByteArray(fileName, output);
        }
    }
}
