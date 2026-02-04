package one.microstream.demo;

import org.eclipse.store.cache.types.CacheConfigurationMXBean;

import javax.cache.configuration.CompleteConfiguration;

public class MyCacheConfigurationMXBean implements CacheConfigurationMXBean
{
    private final CompleteConfiguration<?, ?> configuration;

    public MyCacheConfigurationMXBean(final CompleteConfiguration<?, ?> configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public String getKeyType()
    {
        return this.configuration.getKeyType().getName();
    }

    @Override
    public String getValueType()
    {
        return this.configuration.getValueType().getName();
    }

    @Override
    public boolean isReadThrough()
    {
        return this.configuration.isReadThrough();
    }

    @Override
    public boolean isWriteThrough()
    {
        return this.configuration.isWriteThrough();
    }

    @Override
    public boolean isStoreByValue()
    {
        return this.configuration.isStoreByValue();
    }

    @Override
    public boolean isStatisticsEnabled()
    {
        return this.configuration.isStatisticsEnabled();
    }

    @Override
    public boolean isManagementEnabled()
    {
        return this.configuration.isManagementEnabled();
    }
}
