package one.microstream.demo;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.eclipse.store.storage.types.StorageManager;

@OpenAPIDefinition(
    info = @Info(
        title = "Cluster Storage Demo",
        version = "1.0",
        description = "API for querying a demo database filled with books and authors."
    )
)
public class Application
{
    public static StorageManager SM;

    public static void main(String[] args)
    {
        Micronaut.run(Application.class, args);
    }
}
