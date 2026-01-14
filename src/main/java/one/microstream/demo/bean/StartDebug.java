package one.microstream.demo.bean;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import one.microstream.demo.Application;
import org.eclipse.store.storage.types.StorageManager;

@Singleton
public class StartDebug implements ApplicationEventListener<ApplicationStartupEvent>
{
    @Inject
    StorageManager sm;

    @Override
    public void onApplicationEvent(final ApplicationStartupEvent event)
    {
        System.out.println("LOADING SM");
        Application.SM = sm;
    }
}
