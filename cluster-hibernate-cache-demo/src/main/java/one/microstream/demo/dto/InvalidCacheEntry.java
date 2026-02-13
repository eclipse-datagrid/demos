package one.microstream.demo.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public record InvalidCacheEntry(
    @NonNull String cacheName,
    String idB64,
    String entityOrRoleName,
    String tenantIdentifier,
    Integer entityHashCode,

    String updateTimestampsKey,
    Long updateTimestampsValue
)
{
}
