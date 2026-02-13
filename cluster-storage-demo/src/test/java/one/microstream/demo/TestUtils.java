package one.microstream.demo;

import one.microstream.demo.dto.InsertAuthor;

import java.util.List;
import java.util.stream.IntStream;

public final class TestUtils
{
    public static List<InsertAuthor> generateInsertAuthorNoBooksList(int amount)
    {
        return IntStream.range(0, amount).mapToObj(i ->
        {
            var name = "Author" + i;
            var about = "I am the nr. " + i + " author.";
            return new InsertAuthor(name, about, null);
        }).toList();
    }

    private TestUtils()
    {
    }
}
