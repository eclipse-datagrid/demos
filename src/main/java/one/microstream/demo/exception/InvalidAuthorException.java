package one.microstream.demo.exception;

import java.util.UUID;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class InvalidAuthorException extends HttpStatusException
{
    public InvalidAuthorException(final UUID authorId)
    {
        super(HttpStatus.BAD_REQUEST, "Author with id " + authorId + " does not exist");
    }
}
