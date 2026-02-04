package one.microstream.demo;

import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.store.cache.types.CacheConfiguration;
import org.eclipse.store.cache.types.CacheManager;
import org.eclipse.store.cache.types.CachingProvider;

import javax.cache.CacheException;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.eclipse.serializer.chars.XChars.notEmpty;
import static org.eclipse.serializer.util.X.notNull;

public class MyCacheManager implements CacheManager
{
    private final MyCachingProvider cachingProvider;
    private final URI uri;
    private final WeakReference<ClassLoader> classLoaderReference;
    private final Properties properties;
    private final EqHashTable<String, MyCache<?, ?>> caches = EqHashTable.New();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public MyCacheManager(
        final MyCachingProvider cachingProvider,
        final URI uri,
        final ClassLoader classLoader,
        final Properties properties
    )
    {
        super();

        this.cachingProvider = notNull(cachingProvider);
        this.uri = notNull(uri);
        this.classLoaderReference = new WeakReference<>(notNull(classLoader));
        this.properties = new Properties();
        if (properties != null)
        {
            this.properties.putAll(properties);
        }
    }

    @Override
    public CachingProvider getCachingProvider()
    {
        return this.cachingProvider;
    }

    @Override
    public boolean isClosed()
    {
        return this.isClosed.get();
    }

    @Override
    public URI getURI()
    {
        return this.uri;
    }

    @Override
    public Properties getProperties()
    {
        return this.properties;
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return this.classLoaderReference.get();
    }

    @Override
    public <K, V, C extends Configuration<K, V>> MyCache<K, V>
    createCache(final String cacheName, final C configuration) throws IllegalArgumentException
    {
        notEmpty(cacheName);
        notNull(configuration);

        if (this.getCache(cacheName) != null)
        {
            throw new CacheException("A cache named " + cacheName + " already exists.");
        }

        synchronized (this.caches)
        {
            final MyCache<K, V> cache = new MyCache(
                cacheName,
                this,
                CacheConfiguration.New(configuration)
            );
            this.caches.put(cacheName, cache);
            return cache;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> MyCache<K, V> getCache(final String cacheName)
    {
        this.ensureOpen();

        notNull(cacheName);

        synchronized (this.caches)
        {
            return (MyCache<K, V>)this.caches.get(cacheName);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> MyCache<K, V> getCache(final String cacheName, final Class<K> keyType, final Class<V> valueType)
    {
        this.ensureOpen();

        notNull(keyType);
        notNull(valueType);

        MyCache<K, V> cache;
        synchronized (this.caches)
        {
            cache = (MyCache<K, V>)this.caches.get(notNull(cacheName));
        }
        if (cache == null)
        {
            return null;
        }

        final CompleteConfiguration<K, V> configuration = cache.getConfiguration(CompleteConfiguration.class);
        final Class<K> configuredKeyType = configuration.getKeyType();
        final Class<V> configuredValueType = configuration.getValueType();
        if (configuredKeyType != null && !configuredKeyType.equals(keyType))
        {
            throw new ClassCastException("Incompatible key types: " + keyType + " <> " + configuredKeyType);
        }
        if (configuredValueType != null && !configuredValueType.equals(valueType))
        {
            throw new ClassCastException("Incompatible value types: " + valueType + " <> " + configuredValueType);
        }

        return cache;
    }

    @Override
    public Iterable<String> getCacheNames()
    {
        this.ensureOpen();

        synchronized (this.caches)
        {
            return this.caches.keys().immure();
        }
    }

    @SuppressWarnings("resource")
    @Override
    public void destroyCache(final String cacheName)
    {
        this.ensureOpen();

        MyCache<?, ?> cache;
        synchronized (this.caches)
        {
            cache = this.caches.get(notNull(cacheName));
        }
        if (cache != null)
        {
            cache.close();
        }
    }

    @Override
    public void removeCache(final String cacheName)
    {
        notNull(cacheName);

        synchronized (this.caches)
        {
            this.caches.removeFor(cacheName);
        }
    }

    @Override
    public void enableManagement(final String cacheName, final boolean enabled)
    {
        this.ensureOpen();

        notNull(cacheName);

        synchronized (this.caches)
        {
            this.caches.get(cacheName)
                .setManagementEnabled(enabled);
        }
    }

    @Override
    public void enableStatistics(final String cacheName, final boolean enabled)
    {
        this.ensureOpen();

        notNull(cacheName);

        synchronized (this.caches)
        {
            this.caches.get(cacheName)
                .setStatisticsEnabled(enabled);
        }
    }

    @Override
    public synchronized void close()
    {
        if (this.isClosed.get())
        {
            // no-op, according to spec
            return;
        }

        this.isClosed.set(true);

        this.cachingProvider.remove(
            this.getURI(),
            this.getClassLoader()
        );

        try
        {
            for (final MyCache<?, ?> cache : this.caches.values())
            {
                try
                {
                    cache.close();
                }
                catch (final Exception e)
                {
                    // ignore, according to spec
                }
            }
        }
        finally
        {
            this.caches.clear();
        }
    }

    private void ensureOpen()
    {
        if (this.isClosed.get())
        {
            throw new IllegalStateException("CacheManager is closed");
        }
    }
}
