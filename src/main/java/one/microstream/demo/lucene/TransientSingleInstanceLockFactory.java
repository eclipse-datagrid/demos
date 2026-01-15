package one.microstream.demo.lucene;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.SingleInstanceLockFactory;

import java.io.IOException;

public class TransientSingleInstanceLockFactory extends LockFactory
{
    private transient SingleInstanceLockFactory lockFactory;

    @Override
    public Lock obtainLock(final Directory dir, final String lockName) throws IOException
    {
        if (lockFactory == null)
        {
            lockFactory = new SingleInstanceLockFactory();
        }
        return lockFactory.obtainLock(dir, lockName);
    }
}
