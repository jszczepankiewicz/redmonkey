package dynks.cache;

import com.typesafe.config.ConfigFactory;
import dynks.redis.RedisCacheRepositoryConfigBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

import static dynks.cache.TestValues.ALL_POSSIBLE_HTTP_METHODS;
import static dynks.cache.TestValues.FROM_BOOK_LIST;
import static dynks.cache.TestValues.UTF8_JSON;
import static dynks.cache.test.DynksAssertions.assertThat;

/**
 * Unconfigured (non-registered as regions) urls should bypass credit checks.
 *
 * @author jszczepankiewicz
 * @since 2015-08-09
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigFactory.class, RedisCacheRepositoryConfigBuilder.class, ResponseCacheByURIBuilder.class})
@PowerMockRunnerDelegate(Parameterized.class)
@Ignore
public class UnconfiguredUrlTests {

    protected TestedCache server;

    private String method;

    public UnconfiguredUrlTests(String method) {
        this.method = method;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return ALL_POSSIBLE_HTTP_METHODS;
    }

    protected HttpServletResponse forRequest(String method, HttpServletRequestBuilder request) throws ServletException, IOException {
        return server.forRequest(request);
    }

    protected HttpServletRequestBuilder toBookList() {
        return new HttpServletRequestBuilder(FROM_BOOK_LIST).contentType(UTF8_JSON);
    }

    @Test
    public void unregisteredUrlShouldBypassCache() throws Exception {

        //  server starts with empty configuration

        //  when
        HttpServletResponse response = forRequest(method, toBookList().withEmptyClientCache());

        //  then
        assertThat(response).hasNotBeenAltered();
        assertThat(server).servedResponseByGeneratingIt().hasNotStoredResponseInCache();

    }

    @Before
    public void createServer() {
        server = new TestedCache();
    }

    @After
    public void dispose() {
        server.destroy();
    }


}
