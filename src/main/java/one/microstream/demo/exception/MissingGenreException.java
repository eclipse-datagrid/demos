package one.microstream.demo.exception;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class MissingGenreException extends HttpStatusException
{
    public MissingGenreException(final String genre)
    {
        super(HttpStatus.NOT_FOUND, "Could not find genre '%s'".formatted(genre));
    }
}
