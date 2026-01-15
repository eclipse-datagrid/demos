package one.microstream.demo.lucene;

import org.apache.lucene.store.ByteBuffersDataInput;
import org.apache.lucene.store.ByteBuffersDataOutput;
import org.apache.lucene.store.ByteBuffersIndexInput;
import org.apache.lucene.store.IndexInput;

import java.util.Locale;

public interface OutputAsManyBuffers
{
    IndexInput outputAsManyBuffers(String fileName, ByteBuffersDataOutput output);

    class Default implements OutputAsManyBuffers
    {
        @Override
        public IndexInput outputAsManyBuffers(final String fileName, final ByteBuffersDataOutput output)
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
    }
}
