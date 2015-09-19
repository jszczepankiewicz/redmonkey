package dynks.cache;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static dynks.cache.TestValues.UTF8_JSON;
import static dynks.cache.TestValues.UTF8_PLAIN;

/**
 * @author jszczepankiewicz
 * @since 2015-08-08
 */

public class CachingFilterBaseTest {

    protected static final CachedContent listOfBooksInJson = new CachedContent("etagForListOfBooksJson", "{\"listOfBooks\":[]}", UTF8_JSON);
    protected static final CachedContent listOfBooksInPlainText = new CachedContent("etagForListOfBooksPlain", "[]", UTF8_PLAIN);

    protected static final String FROM_BOOK_LIST = "/api/v1/books";

    protected TestedCache server;



    //  DSL
    protected HttpServletResponse forRequest(String method, HttpServletRequestBuilder request) throws ServletException, IOException {
        return server.forRequest(request);
    }

    protected HttpServletRequestBuilder toBookList(){
        return new HttpServletRequestBuilder(FROM_BOOK_LIST).contentType(UTF8_JSON);
    }
}
