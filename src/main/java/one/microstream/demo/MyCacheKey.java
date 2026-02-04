package one.microstream.demo;

import io.micronaut.core.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public record MyCacheKey(Object id, @NonNull String entityOrRoleName, String tenantId, int entityHashCode)
    implements Serializable
{
    @Override
    public boolean equals(final Object other)
    {
        if (other == null)
        {
            return false;
        }
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof MyCacheKey o))
        {
            return false;
        }
        return Objects.equals(id, o.id)
            && Objects.equals(entityOrRoleName, o.entityOrRoleName)
            && Objects.equals(this.tenantId, o.tenantId);
    }

    @Override
    public int hashCode()
    {
        return entityHashCode;
    }

    @Override
    public String toString()
    {
        // Used to be required for OSCache
        return entityOrRoleName + '#' + id.toString();
    }
}
