package one.microstream.demo.exception;

import java.util.UUID;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class MissingAuthorException extends HttpStatusException
{
    public MissingAuthorException(final UUID authorId)
    {
        super(HttpStatus.NOT_FOUND, "Could not find author with id " + authorId);
    }
}
