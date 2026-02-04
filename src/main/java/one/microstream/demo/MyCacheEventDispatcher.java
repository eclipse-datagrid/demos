package one.microstream.demo;

import org.eclipse.serializer.collections.BulkList;
import org.eclipse.serializer.collections.EqHashTable;
import org.eclipse.serializer.collections.types.XIterable;
import org.eclipse.serializer.collections.types.XList;
import org.eclipse.serializer.collections.types.XTable;
import org.eclipse.serializer.util.cql.CQL;
import org.eclipse.serializer.util.cql.CqlSelection;
import org.eclipse.store.cache.types.CacheEntryListenerRegistration;

import javax.cache.event.*;
import java.util.function.BiConsumer;

import static org.eclipse.serializer.util.X.notNull;

public interface MyCacheEventDispatcher<K, V>
{
    @SuppressWarnings("rawtypes")
    public MyCacheEventDispatcher<K, V> addEvent(
        final Class<? extends CacheEntryListener> listenerClass,
        final MyCacheEvent<K, V> event
    );

    public void dispatch(
        org.eclipse.serializer.collections.types.XIterable<CacheEntryListenerRegistration<K, V>> registrations
    );

    public static <K, V> MyCacheEventDispatcher<K, V> New()
    {
        return new MyCacheEventDispatcher.Default<>();
    }

    public static class Default<K, V> implements MyCacheEventDispatcher<K, V>
    {
        @SuppressWarnings("rawtypes")
        private XTable<Class<? extends CacheEntryListener>, XList<MyCacheEvent<K, V>>> eventMap;

        Default()
        {
            super();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public synchronized MyCacheEventDispatcher<K, V> addEvent(
            final Class<? extends CacheEntryListener> listenerClass,
            final MyCacheEvent<K, V>                    event
        )
        {
            notNull(listenerClass);
            notNull(event);

            // lazy init eventMap
            XTable<Class<? extends CacheEntryListener>, XList<MyCacheEvent<K, V>>> eventMap;
            if((eventMap = this.eventMap) == null)
            {
                eventMap = this.eventMap = EqHashTable.New();
            }
            eventMap
                .ensure(listenerClass, c -> BulkList.New())
                .add(event);

            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public synchronized void dispatch(
            final XIterable<CacheEntryListenerRegistration<K, V>> registrations
        )
        {
            if(this.eventMap == null)
            {
                // no event registered
                return;
            }

            this.dispatch(registrations, CacheEntryExpiredListener.class, CacheEntryExpiredListener::onExpired, true);
            this.dispatch(registrations, CacheEntryCreatedListener.class, CacheEntryCreatedListener::onCreated, false);
            this.dispatch(registrations, CacheEntryUpdatedListener.class, CacheEntryUpdatedListener::onUpdated, true);
            this.dispatch(registrations, CacheEntryRemovedListener.class, CacheEntryRemovedListener::onRemoved, true);
        }

        @SuppressWarnings("unchecked")
        private <L extends CacheEntryListener<? super K, ? super V>> void dispatch(
            final XIterable<CacheEntryListenerRegistration<K, V>>                    registrations,
            final Class<L>                                                           type,
            final BiConsumer<L, Iterable<CacheEntryEvent<? extends K, ? extends V>>> logic,
            final boolean                                                            oldValueAvailable
        )
        {
            final XList<MyCacheEvent<K, V>> events;
            if((events = this.eventMap.get(type)) != null)
            {
                registrations.iterate(registration ->
                {
                    final CacheEntryListener<? super K, ? super V> listener;
                    if(type.isInstance(listener = registration.getCacheEntryListener()))
                    {
                        logic.accept(
                            type.cast(listener),
                            this.selectEvents(registration, events, oldValueAvailable)
                        );
                    }
                });
            }
        }

        @SuppressWarnings("rawtypes")
        private Iterable selectEvents(
            final CacheEntryListenerRegistration<K, V> registration,
            final XList<MyCacheEvent<K, V>>              events,
            final boolean                              oldValueAvailable
        )
        {
            CqlSelection<MyCacheEvent<K, V>> selection = CQL.from(events);
            final CacheEntryEventFilter<? super K, ? super V> filter;
            if((filter = registration.getCacheEntryFilter()) != null)
            {
                selection = selection.select(e -> filter.evaluate(e));
            }
            return selection
                .project(e -> this.cloneEvent(registration, e, oldValueAvailable))
                .into(BulkList.New())
                .execute();
        }

        private MyCacheEvent<K, V> cloneEvent(
            final CacheEntryListenerRegistration<K, V> registration,
            final MyCacheEvent<K, V>                     event,
            final boolean                              oldValueAvailable
        )
        {
            if(oldValueAvailable && registration.isOldValueRequired())
            {
                return new MyCacheEvent<>(
                    event.getCache(),
                    event.getEventType(),
                    event.getKey(),
                    event.getValue(),
                    event.getOldValue()
                );
            }

            if(event.getEventType() == EventType.REMOVED || event.getEventType() == EventType.EXPIRED)
            {
                return new MyCacheEvent<>(
                    event.getCache(),
                    event.getEventType(),
                    event.getKey(),
                    null
                );
            }

            return new MyCacheEvent<>(
                event.getCache(),
                event.getEventType(),
                event.getKey(),
                event.getValue()
            );
        }

    }
}
