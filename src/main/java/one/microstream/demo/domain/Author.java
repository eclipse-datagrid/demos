package one.microstream.demo.domain;

import java.util.Set;
import java.util.UUID;

import org.eclipse.serializer.reference.Lazy;

/**
 * @param id    the unique identifier
 * @param name  the name of the author
 * @param about some information about the author
 * @param books the books the author has written
 */
public record Author(UUID id, String name, String about, Lazy<Set<Book>> books)
{
}
