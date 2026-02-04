package one.microstream.demo;

import org.eclipse.store.cache.types.CacheManager;
import org.eclipse.store.cache.types.CachingProvider;

import javax.cache.configuration.OptionalFeature;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

public class MyCachingProvider extends CachingProvider
{
    public static URI defaultURI()
    {
        try
        {
            return new URI("eclipsestore");
        }
        catch(final URISyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }

    private final WeakHashMap<ClassLoader, HashMap<URI, MyCacheManager>> cacheManagers = new WeakHashMap<>();

    @Override
    public ClassLoader getDefaultClassLoader()
    {
        return this.getClass().getClassLoader();
    }

    @Override
    public URI getDefaultURI()
    {
        return defaultURI();
    }

    @Override
    public Properties getDefaultProperties()
    {
        return null;
    }

    @Override
    public MyCacheManager getCacheManager()
    {
        return this.getCacheManager(
            this.getDefaultURI(),
            this.getDefaultClassLoader()
        );
    }

    @Override
    public MyCacheManager getCacheManager(
    final URI uri,
    final ClassLoader classLoader
	)
    {
        return this.getCacheManager(
            uri,
            classLoader,
            this.getDefaultProperties()
        );
    }

    @Override
    public synchronized MyCacheManager getCacheManager(
    final URI uri,
    final ClassLoader classLoader,
    final Properties properties
	)
    {
        final URI         managerURI         = uri == null
                                               ? this.getDefaultURI()
                                               : uri;

        final ClassLoader managerClassLoader = classLoader == null
                                               ? this.getDefaultClassLoader()
                                               : classLoader;

        final Properties  managerProperties  = properties == null
                                               ? new Properties()
                                               : properties;

        return this.cacheManagers.computeIfAbsent(managerClassLoader, cl -> new HashMap<>())
            .computeIfAbsent(managerURI,
                key -> new MyCacheManager(this, managerURI, managerClassLoader, managerProperties)
            );
    }

    @Override
    public synchronized void close()
    {
        /*
         * Collect to list because CacheManager#close modifies this#cacheManagers
         */
        final List<CacheManager> managers = this.cacheManagers.values().stream()
            .flatMap(kv -> kv.values().stream())
            .collect(Collectors.toList());
        managers.forEach(CacheManager::close);

        this.cacheManagers.clear();
    }

    @Override
    public synchronized void close(final ClassLoader classLoader)
    {
        final ClassLoader                managerClassLoader = classLoader == null
                                                              ? this.getDefaultClassLoader()
                                                              : classLoader;

        final HashMap<URI, MyCacheManager> cacheManagersByURI;
        if((cacheManagersByURI = this.cacheManagers.remove(managerClassLoader)) != null)
        {
            cacheManagersByURI.values()
                .forEach(CacheManager::close);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public synchronized void close(final URI uri, final ClassLoader classLoader)
    {
        final URI                        managerURI         = uri == null
                                                              ? this.getDefaultURI()
                                                              : uri;

        final ClassLoader                managerClassLoader = classLoader == null
                                                              ? this.getDefaultClassLoader()
                                                              : classLoader;

        final HashMap<URI, MyCacheManager> cacheManagersByURI;
        if((cacheManagersByURI = this.cacheManagers.get(managerClassLoader)) != null)
        {
            final MyCacheManager cacheManager;
            if((cacheManager = cacheManagersByURI.remove(managerURI)) != null)
            {
                cacheManager.close();

                if(cacheManagersByURI.size() == 0)
                {
                    this.cacheManagers.remove(managerClassLoader);
                }
            }
        }
    }

    synchronized void remove(final URI uri, final ClassLoader classLoader)
    {
        final HashMap<URI, MyCacheManager> cacheManagersByURI;
        if((cacheManagersByURI = this.cacheManagers.get(classLoader)) != null)
        {
            if(cacheManagersByURI.remove(uri) != null && cacheManagersByURI.size() == 0)
            {
                this.cacheManagers.remove(classLoader);
            }
        }
    }

    @Override
    public boolean isSupported(final OptionalFeature optionalFeature)
    {
        switch(optionalFeature)
        {
        case STORE_BY_REFERENCE:
            return true;

        default:
            return false;
        }
    }

}
