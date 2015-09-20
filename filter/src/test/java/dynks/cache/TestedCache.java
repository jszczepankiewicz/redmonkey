package dynks.cache;

import com.typesafe.config.ConfigFactory;
import dynks.redis.RedisCacheRepository;
import dynks.redis.RedisCacheRepositoryConfigBuilder;
import org.mockito.ArgumentMatcher;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import dynks.CachingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.DAYS;
import static org.assertj.core.util.Preconditions.checkNotNullOrEmpty;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.slf4j.LoggerFactory.getLogger;
import static dynks.cache.ResponseCacheByURIPolicy.PASSTHROUGH;
import static dynks.cache.TestValues.TEST_NAMESPACE;

/**
 * Part of test infrastructure. Class containing state / mocks on server side.
 *
 * @author jszczepankiewicz
 * @since 2015-06-14
 */
public class TestedCache {

    public static final int TEST_CACHE_TTL = 999;
    public static final TimeUnit TEST_CACHE_TTL_UNIT = DAYS;

    private static final Logger LOG = getLogger("TEST");
    private CachingFilter filter;

    private Set<String> proxiedUrls = new HashSet<>();
    private Set<String> cacheableUrls = new HashSet<>();

    private CachingFilter cachingFilter = new CachingFilter();

    private RedisCacheRepository repoMock = mock(RedisCacheRepository.class);
    private ResponseCacheByURIPolicy policyMock = mock(ResponseCacheByURIPolicy.class);

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChainMock;
    private ServletOutputStream outputStreamMock;
    private byte[] responseWritten;

    public byte[] getResponseWritten() {
        return responseWritten;
    }

    public void setResponseWritten(byte[] responseWritten) {
        this.responseWritten = responseWritten;
    }

    public RedisCacheRepository getRepoMock() {
        return repoMock;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public FilterChain getFilterChainMock() {
        return filterChainMock;
    }

    public ServletOutputStream getOutputStreamMock() {
        return outputStreamMock;
    }

    /**
     * Once set to true should modification of state for mocks is impossible
     */
    private boolean initialized = false;

    private HttpServletResponse createResponse() throws IOException {
        if(response == null){
            response = mock(HttpServletResponse.class);
            outputStreamMock = mock(ServletOutputStream.class);

            when(response.getOutputStream()).thenReturn(outputStreamMock);
        }

        return response;
    }
    public HttpServletResponse forRequest(HttpServletRequestBuilder requestBuilder) throws ServletException, IOException {

        initializeOnce();

        response = mock(HttpServletResponse.class);
        request = requestBuilder.build();

        filter.doFilter(request, response, filterChainMock);
        return response;
    }

    public void hasProxiedUrl(String proxiedUrl) {

        assertNotInitializedYet();
        checkNotNullOrEmpty(proxiedUrl);
        proxiedUrls.add(proxiedUrl);
    }

    public void hasCacheableUrl(String cacheableUrl){

        assertNotInitializedYet();
        checkNotNullOrEmpty(cacheableUrl);
        cacheableUrls.add(cacheableUrl);
    }

    private void assertNotInitializedYet(){
        if(initialized){
            throw new IllegalStateException("TestedCache is closed for modification. You are trying to alter configuration after all is prepared for testing or you are reusing TestedCache between test which is prohibited.");
        }
    }

    public void destroy(){
        if (filter != null) {
            filter.destroy();
        }
    }

    /**
     * Initializing state of server for tests according to earlier contract.
     * If pre-test state / mocks are not yet initialized they will be. Otherwise silently do nothing.
     */
    private void initializeOnce() throws ServletException {

        if(initialized){
            return;
        }

        filter = new CachingFilter();

        mockStatic(ConfigFactory.class);
        mockStatic(RedisCacheRepositoryConfigBuilder.class);
        mockStatic(ResponseCacheByURIBuilder.class);

        repoMock = mock(RedisCacheRepository.class);
        policyMock = mock(ResponseCacheByURIPolicy.class);
        filterChainMock = mock(FilterChain.class);

        PowerMockito.when(ConfigFactory.load("redmonkey")).thenReturn(null);
        PowerMockito.when(RedisCacheRepositoryConfigBuilder.build(anyObject())).thenReturn(repoMock);
        PowerMockito.when(ResponseCacheByURIBuilder.build(anyObject())).thenReturn(policyMock);

        initializeProxiedUrls();
        initializeCacheableUrls();

        filter.init(null);

        //  now we should be ready to  WHEN phase of thest when servlet.filter will be invoked

    }

    private void initializeCacheableUrls(){

        if(cacheableUrls.size()==0){
            return;
        }

        for(String url:cacheableUrls){
            CacheRegion region = new CacheRegion(TEST_CACHE_TTL, TEST_CACHE_TTL_UNIT, new NamespacedURIKeyStrategy(TEST_NAMESPACE));
            when(policyMock.getfor(argThat(new ArgumentMatcher<HttpServletRequest>(){
                @Override
                public boolean matches(Object argument) {
                    System.out.println("=== " + request.getRequestURI());
                    HttpServletRequest request = (HttpServletRequest)argument;
                    return url.equalsIgnoreCase(request.getRequestURI());
                }
            }))).thenReturn(region);
            LOG.debug("Added cacheable url: {}", url);
        }

    }

    private void initializeProxiedUrls(){

        if(proxiedUrls.size() == 0){
            return;
        }

        for(String url:proxiedUrls){
            when(policyMock.getfor(argThat(new ArgumentMatcher<HttpServletRequest>(){
                @Override
                public boolean matches(Object argument) {
                    HttpServletRequest request = (HttpServletRequest)argument;
                    return url.equalsIgnoreCase(request.getRequestURI());
                }
            }))).thenReturn(PASSTHROUGH);
            LOG.debug("Added proxied url: {}", url);
        }
    }


}
