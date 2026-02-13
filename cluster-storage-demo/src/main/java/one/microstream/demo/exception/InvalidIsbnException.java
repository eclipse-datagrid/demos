package one.microstream.demo.exception;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;

public class InvalidIsbnException extends HttpStatusException
{
    public InvalidIsbnException(final String isbn)
    {
        super(HttpStatus.BAD_REQUEST, "Book with ISBN '%s' already exists".formatted(isbn));
    }
}
