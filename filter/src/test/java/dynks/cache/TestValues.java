package dynks.cache;

import java.util.Collection;

import static java.util.Arrays.asList;
import static dynks.http.HttpMethod.*;
import static dynks.http.HttpMethod.HEAD;
import static dynks.http.HttpMethod.OPTIONS;

/**
 * @author jszczepankiewicz
 * @since 2015-05-30
 */
public class TestValues {

    public static final String UTF8_JSON = "application/json; charset=utf-8";
    public static final String UTF8_PLAIN = "text/plain; charset=utf-8";

    public static final String TEST_NAMESPACE = "rm-tst";

    protected static final CachedContent listOfBooksInJson = new CachedContent("etagForListOfBooksJson", "{\"listOfBooks\":[]}", UTF8_JSON);
    protected static final CachedContent listOfBooksInPlainText = new CachedContent("etagForListOfBooksPlain", "[]", UTF8_PLAIN);

    protected static final String FROM_BOOK_LIST = "/api/v1/books";

    public static final Collection<Object[]> ALL_POSSIBLE_HTTP_METHODS = asList(new Object[][]{{GET}, {POST}, {PUT}, {DELETE}, {HEAD}, {OPTIONS}, {"SOME_UNINVENTED"}});

    private TestValues() {
        //  instances not allowed
    }
}
