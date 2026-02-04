package one.microstream.demo;

import org.hibernate.cache.internal.NaturalIdCacheKey;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

import java.io.Serializable;

public class MyCacheKeysFactory implements CacheKeysFactory
{
    public static final MyCacheKeysFactory INSTANCE = new MyCacheKeysFactory();

    @Override
    public Object createCollectionKey(
        final Object id,
        final CollectionPersister persister,
        final SessionFactoryImplementor factory,
        final String tenantIdentifier
    )
    {
        return createKey(id, persister.getKeyType(), persister.getRole(), factory, tenantIdentifier);
    }

    @Override
    public Object createEntityKey(
        final Object id,
        final EntityPersister persister,
        final SessionFactoryImplementor factory,
        final String tenantIdentifier
    )
    {
        return createKey(
            id,
            persister.getIdentifierType(),
            persister.getRootEntityName(),
            factory,
            tenantIdentifier
        );
    }

    @Override
    public Object createNaturalIdKey(
        final Object naturalIdValues,
        final EntityPersister persister,
        final SharedSessionContractImplementor session
    )
    {
        return NaturalIdCacheKey.from(naturalIdValues, persister, session);
    }

    @Override
    public Object getEntityId(final Object cacheKey)
    {
        if (!(cacheKey instanceof MyCacheKey k))
        {
            throw new RuntimeException("Only MyCacheKey are supported");
        }
        return k.id();
    }

    @Override
    public Object getCollectionId(final Object cacheKey)
    {
        if (!(cacheKey instanceof MyCacheKey k))
        {
            throw new RuntimeException("Only MyCacheKey are supported");
        }
        return k.id();
    }

    @Override
    public Object getNaturalIdValues(final Object cacheKey)
    {
        return ((NaturalIdCacheKey)cacheKey).getNaturalIdValues();
    }

    private Object createKey(
        final Object id,
        final Type keyType,
        final String entityOrRoleName,
        final SessionFactoryImplementor factory,
        final String tenantIdentifier
    )
    {
        final Serializable disassembledKey = keyType.disassemble(id, factory);
        final boolean idIsArray = disassembledKey.getClass().isArray();
        final int hashCode;
        if (tenantIdentifier == null && !idIsArray)
        {
            hashCode = calculateHashCode(id, keyType);
        }
        else
        {
            hashCode = calculateHashCode(id, keyType, tenantIdentifier);
        }
        return new MyCacheKey(disassembledKey, entityOrRoleName, tenantIdentifier, hashCode);
    }

    private static int calculateHashCode(Object disassembledKey, Type type)
    {
        return type.getHashCode(disassembledKey);
    }

    private static int calculateHashCode(Object id, Type type, String tenantId)
    {
        int result = type.getHashCode(id);
        result = 31 * result + (tenantId != null ? tenantId.hashCode() : 0);
        return result;
    }
}
