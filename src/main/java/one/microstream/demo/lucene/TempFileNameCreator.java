package one.microstream.demo.lucene;

import java.util.concurrent.atomic.AtomicLong;

public interface TempFileNameCreator
{
    String createTempFileName(String suffix);

    class Default implements TempFileNameCreator
    {
        private final AtomicLong counter = new AtomicLong();

        @Override
        public String createTempFileName(String suffix)
        {
            return suffix + "_" + Long.toString(counter.getAndIncrement(), Character.MAX_RADIX);
        }
    }
}
