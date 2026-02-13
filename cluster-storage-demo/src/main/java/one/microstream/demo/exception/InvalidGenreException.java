package one.microstream.demo.exception;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class InvalidGenreException extends HttpStatusException
{
    public static InvalidGenreException doesNotExist(final String genre)
    {
        return new InvalidGenreException("Genre '%s' does not exist".formatted(genre));
    }

    public InvalidGenreException(final String message)
    {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
