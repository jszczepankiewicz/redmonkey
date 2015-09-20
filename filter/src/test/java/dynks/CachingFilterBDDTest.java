package dynks;

import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import dynks.cache.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static dynks.cache.test.DynksAssertions.assertThat;
import static dynks.cache.TestValues.UTF8_JSON;
import static dynks.cache.TestValues.UTF8_PLAIN;
import static dynks.http.HttpMethod.GET;


/**
 * Main caching filter test. Due to high amount of mocking behaviour attempted to getProbe simple DSL to simplify testing.
 *
 * @author jszczepankiewicz
 * @since 2015-06-14
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigFactory.class, RedisCacheRepositoryConfigBuilder.class, ResponseCacheByURIBuilder.class})
public class CachingFilterBDDTest {

    private static final CachedContent listOfBooksInJson = new CachedContent("etagForListOfBooksJson", "{\"listOfBooks\":[]}", UTF8_JSON);
    private static final CachedContent listOfBooksInPlainText = new CachedContent("etagForListOfBooksPlain", "[]", UTF8_PLAIN);

    private static final String FROM_BOOK_LIST = "/api/v1/books";

    private TestedCache server;

    public HttpServletResponse forRequest(String method, HttpServletRequestBuilder request) throws ServletException, IOException {
        return server.forRequest(request);
    }

    @Before
    public void createServer() {
        server = new TestedCache();
    }

    @After
    public void dispose() {
        server.destroy();
    }

    //TODO: duplicate, remove
    @Test
    public void proxiedRegionShouldBypassCache() throws Exception {

        //  given
        server.hasProxiedUrl(FROM_BOOK_LIST);

        //  when
        HttpServletResponse response = forRequest(GET, toBookList().withEmptyClientCache());

        //  then
        assertThat(response).hasNotBeenAltered();
        assertThat(server).servedResponseByGeneratingIt().hasNotStoredResponseInCache();

    }

    @Test
    public void notAccessedYetByTheClientAndNotYetCachedShouldBeGeneratedAndStoredToCache() throws Exception {

        //  given
        server.hasCacheableUrl(FROM_BOOK_LIST);

        //  when
        HttpServletResponse response = server.forRequest(getUTF8Json(FROM_BOOK_LIST).withEmptyClientCache());

        //  then
        assertThat(response).containsCacheable(listOfBooksInJson);
        assertThat(server).servedResponseByGeneratingIt().hasStoredResponseInCache();

    }

    //  add test that will invalidate cache if suddenly region becomes proxy (adn client still has it on client cache)

    /*
    @Test
    public void throwingExceptionFromUnderlyingServletShouldBeRethrownLater() throws Exception {

        //  given & then
        server.willThrowExceptionFromGeneratingServlet();


        //  when
        HttpServletResponse response = server.forRequest(getUTF8Json(FROM_BOOK_LIST).withCachedOnClient(PREVIOUS_VERSION));

    }

    @Test
    public void postRequestShouldBypassCache() throws Exception {

        //  given
        server.hasProxiedUrl(FROM_BOOK_LIST);

        //  when
        HttpServletResponse response = server.forRequest(postUTF8Json(FROM_BOOK_LIST).withEmptyClientCache());

        //  then
        assertThat(response).containsNonCacheable(listOfBooksInJson);
        assertThat(server).servedResponseByGeneratingWithoutStoringInCache();
    }

    @Test
    public void putRequestsShouldBypassCache() throws Exception {

        //  given
        server.hasProxiedUrl(FROM_BOOK_LIST);

        //  when
        HttpServletResponse response = server.forRequest(putUTF8Json(FROM_BOOK_LIST).withEmptyClientCache());

        //  then
        assertThat(response).containsNonCacheable(listOfBooksInJson);
        assertThat(server).servedResponseByGeneratingWithoutStoringInCache();
    }

    @Test
    public void deleteRequestsShouldBypassCache() throws Exception {

        //  given
        server.hasProxiedUrl(FROM_BOOK_LIST);

        //  when
        HttpServletResponse response = server.forRequest(deleteUTF8Json(FROM_BOOK_LIST).withEmptyClientCache());

        //  then
        assertThat(response).containsNonCacheable(listOfBooksInJson);
        assertThat(server).servedResponseByGeneratingWithoutStoringInCache();
    }




    @Test
    public void clientRequestingChangedCachedResourceShouldRetrieveResourceFromCache() throws Exception {

        //  given
        server.hasCached(listOfBooksInJson).forUrl(FROM_BOOK_LIST);

        //  when
        HttpServletResponse response = server.forRequest(getUTF8Json(FROM_BOOK_LIST).withCachedOnClient(listOfBooksInJson));

        //  then
        assertThat(server).servedResponseByReadingFromCacheOnly();
        assertThat(response).hasNotChanged();
    }


    @Test
    public void clientRequestingExpiredResourceShouldHaveResourceGeneratedAndCached() throws Exception {

        //  given
        server.hasCacheable(listOfBooksInJson).forUrl(FROM_BOOK_LIST);

        //  when
        HttpServletResponse response = server.forRequest(getUTF8Json(FROM_BOOK_LIST).withCachedOnClient(PREVIOUS_VERSION));

        //  then
        assertThat(server).servedResponseByGeneratingAndSavingInCache();

    }


    @Test
    public void notAccessedYetByTheClientAndNotYetCachedShouldBeGeneratedAndStoredToCache() throws Exception {

        //  given
        server.withEmptyCache();

        //  when
        HttpServletResponse response = server.forRequest(getUTF8Json(FROM_BOOK_LIST).withEmptyClientCache());

        //  then
        assertThat(response).containsCacheable(listOfBooksInJson);
        assertThat(server).servedResponseByGeneratingAndSavingInCache();

    }

    @Test
    public void notAccessedYetByTheClientButCachedServiceShouldBeReturnedFromCache() throws Exception {

        //  given
        server.hasCached(listOfBooksInJson).forUrl(FROM_BOOK_LIST);


        //  when
        HttpServletResponse response = server.forRequest(getUTF8Json(FROM_BOOK_LIST).withEmptyClientCache());

        //  then
        assertThat(response).containsCacheable(listOfBooksInJson);

        //  assert no underlying servlet was invoked and everything was from under server
        assertThat(server).servedResponseByReadingFromCacheOnly();

    }
    */

    private HttpServletRequestBuilder toBookList(){
        return new HttpServletRequestBuilder(FROM_BOOK_LIST).contentType(UTF8_JSON);
    }

    private HttpServletRequestBuilder utf8Json(String url){
        return new HttpServletRequestBuilder(url).contentType(UTF8_JSON);
    }
    //  state objects used in tests
    private HttpServletRequestBuilder getUTF8Json(String url) {
        return new HttpServletRequestBuilder(url, GET).contentType(UTF8_JSON);
    }


}