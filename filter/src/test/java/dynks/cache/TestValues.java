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

    private TestValues() {
        //  instances not allowed
    }
}
