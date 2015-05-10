package badmonkey;


import badmonkey.cache.*;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static badmonkey.cache.ETag.ETAG_RESPONSE_HEADER;
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static badmonkey.BadmonkeyLibraryAssertions.assertThat;

/**
 * TODO: test flow should be refactored to more object oriented way thus eliminating passing so many arguments
 * TODO: add buldier pattern at least to request
 * There should be minimum object passing between methods of setup and assertions (its doable when chaining client & response)
 * maybe using "application" object.
 * TODO: there should be no direct usage of mockito in tests, hide it with ultra readable DSL.
 *
 * to DSL assertion methods.
 *
 * @author jszczepankiewicz
 * @since 2015-04-21
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigFactory.class, RedisCacheRepositoryConfigBuilder.class, ResponseCacheByURIBuilder.class})
public class CachingFilterTest {

    private static final String CACHEABLE_URI = "/api/v1/customer/123";
    private CachingFilter filter;


    @Ignore("implement me")
    @Test
    public void notAccessedByAnyClientServiceShouldBeGeneratedAndPutIntoCache() {


    }

    @Ignore("implement me")
    @Test
    public void notAccessedYetByTheClientButCachedServiceShouldBeReturnedFromCache() {

    }

    @Test
    public void clientHavingOlderVersionThatExistInCacheShouldGetIt() throws Exception{

        //  given
        mockStatic(ConfigFactory.class);
        mockStatic(RedisCacheRepositoryConfigBuilder.class);
        mockStatic(ResponseCacheByURIBuilder.class);

        HttpServletRequest request = doingGET(requestToCacheableURI());
        ServletOutputStream outputStreamMock = mock(ServletOutputStream.class);
        //HttpServletResponse response = response();
        //  TODO: can we just hide creating responseStream inside response mock creation?
        HttpServletResponse response = responseWith(outputStreamMock);

        RedisCacheRepository repoMock = mock(RedisCacheRepository.class);
        ResponseCacheByURIPolicy policyMock = mock(ResponseCacheByURIPolicy.class);

        when(policyMock.getfor(request)).thenReturn(cacheableRegion());
        CacheResult cachedContent = storedValueDifferent();
        when(repoMock.fetchIfChanged(anyString(), anyString())).thenReturn(cachedContent);

        PowerMockito.when(ConfigFactory.load("redmonkey")).thenReturn(null);
        PowerMockito.when(RedisCacheRepositoryConfigBuilder.build(anyObject())).thenReturn(repoMock);
        PowerMockito.when(ResponseCacheByURIBuilder.build(anyObject())).thenReturn(policyMock);
        FilterChain filterChainMock = withFilterChain();

        filter.init(null);

        //  when
        filter.doFilter(request, response, filterChainMock);

        //  then

        //  pass directly cachedContent object so it can also verify etag written?
        verifyResponseWritten(response, outputStreamMock, cachedContent.getPayload());

        //  verify etag header set
        //  TODO: add this assertion to other tests?
        verifyEtagSet(response, cachedContent.getStoredEtag());

        //  verify no interaction with underlying servlets
        verifyRequestNotPassedToApplication(filterChainMock, request, response);
    }

    @Test
    public void clientRefreshingPageThatHasNotChangedShouldGetHttp304withoutContent() throws Exception {

        //  given
        mockStatic(ConfigFactory.class);
        mockStatic(RedisCacheRepositoryConfigBuilder.class);
        mockStatic(ResponseCacheByURIBuilder.class);

        HttpServletRequest request = doingGET(requestToCacheableURI());
        HttpServletResponse response = response();

        RedisCacheRepository repoMock = mock(RedisCacheRepository.class);
        ResponseCacheByURIPolicy policyMock = mock(ResponseCacheByURIPolicy.class);

        when(policyMock.getfor(request)).thenReturn(cacheableRegion());
        when(repoMock.fetchIfChanged(anyString(), anyString())).thenReturn(storedValueUnchanged());

        PowerMockito.when(ConfigFactory.load("redmonkey")).thenReturn(null);
        PowerMockito.when(RedisCacheRepositoryConfigBuilder.build(anyObject())).thenReturn(repoMock);
        PowerMockito.when(ResponseCacheByURIBuilder.build(anyObject())).thenReturn(policyMock);

        filter.init(null);

        //  when
        filter.doFilter(request, response, withFilterChain());

        //  then
        //  assert response code
        verify(response, times(1)).setStatus(SC_NOT_MODIFIED);

        //  assert body empty
        assertEmptyBodyReturnedToClient(response);

    }


    @Test
    public void shouldPassthroughOnNonCacheableURI() throws Exception {

        //  given
        mockStatic(ConfigFactory.class);
        mockStatic(RedisCacheRepositoryConfigBuilder.class);
        mockStatic(ResponseCacheByURIBuilder.class);

        HttpServletRequest request = doingGET(requestToCacheableURI());
        HttpServletResponse response = response();

        RedisCacheRepository repoMock = mock(RedisCacheRepository.class);
        ResponseCacheByURIPolicy policyMock = mock(ResponseCacheByURIPolicy.class);
        FilterChain filterChainMock = withFilterChain();

        when(policyMock.getfor(request)).thenReturn(nonCacheableRegion());
        when(repoMock.fetchIfChanged(anyString(), anyString())).thenReturn(storedValueUnchanged());

        PowerMockito.when(ConfigFactory.load("redmonkey")).thenReturn(null);
        PowerMockito.when(RedisCacheRepositoryConfigBuilder.build(anyObject())).thenReturn(repoMock);
        PowerMockito.when(ResponseCacheByURIBuilder.build(anyObject())).thenReturn(policyMock);

        filter.init(null);

        //  when
        filter.doFilter(request, response, filterChainMock);

        //  then
        verify(filterChainMock, times(1)).doFilter(request, response);
        //  should not even touch the cache repository nor do something with reponse
        verifyZeroInteractions(repoMock, response);

    }

    @Test
    public void shouldPassthroughOnPost() throws Exception{

        //  given
        mockStatic(ConfigFactory.class);
        mockStatic(RedisCacheRepositoryConfigBuilder.class);
        mockStatic(ResponseCacheByURIBuilder.class);

        HttpServletRequest request = doingPOST(requestToCacheableURI());
        HttpServletResponse response = response();

        RedisCacheRepository repoMock = mock(RedisCacheRepository.class);
        ResponseCacheByURIPolicy policyMock = mock(ResponseCacheByURIPolicy.class);
        FilterChain filterChainMock = withFilterChain();

        when(policyMock.getfor(request)).thenReturn(cacheableRegion());
        when(repoMock.fetchIfChanged(anyString(), anyString())).thenReturn(storedValueUnchanged());

        PowerMockito.when(ConfigFactory.load("redmonkey")).thenReturn(null);
        PowerMockito.when(RedisCacheRepositoryConfigBuilder.build(anyObject())).thenReturn(repoMock);
        PowerMockito.when(ResponseCacheByURIBuilder.build(anyObject())).thenReturn(policyMock);

        filter.init(null);

        //  when
        filter.doFilter(request, response, filterChainMock);

        //  then
        verify(filterChainMock, times(1)).doFilter(request, response);
        //  should not even touch the cache repository nor do something with reponse
        verifyZeroInteractions(repoMock, response);
    }

    @Test
    public void shouldPassthroughOnPut() throws Exception{

        //  given
        mockStatic(ConfigFactory.class);
        mockStatic(RedisCacheRepositoryConfigBuilder.class);
        mockStatic(ResponseCacheByURIBuilder.class);

        HttpServletRequest request = doingPUT(requestToCacheableURI());
        HttpServletResponse response = response();

        RedisCacheRepository repoMock = mock(RedisCacheRepository.class);
        ResponseCacheByURIPolicy policyMock = mock(ResponseCacheByURIPolicy.class);
        FilterChain filterChainMock = withFilterChain();

        when(policyMock.getfor(request)).thenReturn(cacheableRegion());
        when(repoMock.fetchIfChanged(anyString(), anyString())).thenReturn(storedValueUnchanged());

        PowerMockito.when(ConfigFactory.load("redmonkey")).thenReturn(null);
        PowerMockito.when(RedisCacheRepositoryConfigBuilder.build(anyObject())).thenReturn(repoMock);
        PowerMockito.when(ResponseCacheByURIBuilder.build(anyObject())).thenReturn(policyMock);

        filter.init(null);

        //  when
        filter.doFilter(request, response, filterChainMock);

        //  then
        verify(filterChainMock, times(1)).doFilter(request, response);
        //  should not even touch the cache repository nor do something with reponse
        verifyZeroInteractions(repoMock, response);
    }

    @Test
    public void shouldPassthroughOnDelete() throws Exception {

        //  given
        mockStatic(ConfigFactory.class);
        mockStatic(RedisCacheRepositoryConfigBuilder.class);
        mockStatic(ResponseCacheByURIBuilder.class);

        HttpServletRequest request = doingDELETE(requestToCacheableURI());
        HttpServletResponse response = response();

        RedisCacheRepository repoMock = mock(RedisCacheRepository.class);
        ResponseCacheByURIPolicy policyMock = mock(ResponseCacheByURIPolicy.class);
        FilterChain filterChainMock = withFilterChain();

        when(policyMock.getfor(request)).thenReturn(cacheableRegion());
        when(repoMock.fetchIfChanged(anyString(), anyString())).thenReturn(storedValueUnchanged());

        PowerMockito.when(ConfigFactory.load("redmonkey")).thenReturn(null);
        PowerMockito.when(RedisCacheRepositoryConfigBuilder.build(anyObject())).thenReturn(repoMock);
        PowerMockito.when(ResponseCacheByURIBuilder.build(anyObject())).thenReturn(policyMock);

        filter.init(null);

        //  when
        filter.doFilter(request, response, filterChainMock);

        //  then
        verify(filterChainMock, times(1)).doFilter(request, response);
        //  should not even touch the cache repository nor do something with reponse
        verifyZeroInteractions(repoMock, response);
    }

    /**
     * Checks whether response code SC_OK (200) is set, there was exactly once attempt to write content to outputStreamMock.
     *
     * @param responseMock
     * @param outputStreamMock
     * @param content
     * @throws IOException
     */
    private void verifyResponseWritten(HttpServletResponse responseMock, ServletOutputStream outputStreamMock, String content) throws IOException {

        verify(responseMock, times(1)).setStatus(SC_OK);

        ArgumentCaptor<byte[]> responseContentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStreamMock, times(1)).write(responseContentCaptor.capture());

        assertThat(responseContentCaptor.getValue()).isEqualTo(content.getBytes());
    }

    private CacheResult storedValueDifferent(){
        CacheResult result = new CacheResult(false, "{somepayload}", "assumedNewerEtag");
        return result;
    }

    private CacheResult storedValueUnchanged() {
        CacheResult result = new CacheResult(false, null, null);
        return result;
    }

    private FilterChain withFilterChain() {
        FilterChain chain = mock(FilterChain.class);
        return chain;
    }


    private void assertEmptyBodyReturnedToClient(HttpServletResponse response) throws IOException {
        verify(response, never()).getOutputStream();
        verify(response, never()).getWriter();
    }

    private HttpServletResponse response() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        return response;
    }

    private HttpServletResponse responseWith(ServletOutputStream outputStreamMock) throws IOException {
        HttpServletResponse response = response();
        when(response.getOutputStream()).thenReturn(outputStreamMock);
        return response;
    }

    private HttpServletRequest requestToCacheableURI() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(CACHEABLE_URI);
        return request;
    }

    private HttpServletRequest requestTo(String url) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(url);
        return request;
    }

    private CacheRegion nonCacheableRegion() {
        return ResponseCacheByURIPolicy.PASSTHROUGH;
    }

    private CacheRegion cacheableRegion() {
        CacheRegion region = new CacheRegion(999, TimeUnit.DAYS, new URIKeyStrategy());
        return region;
    }

    private HttpServletRequest doingDELETE(HttpServletRequest request){
        when(request.getMethod()).thenReturn("DELETE");
        return request;
    }

    private HttpServletRequest doingPOST(HttpServletRequest request){
        when(request.getMethod()).thenReturn("POST");
        return request;
    }

    private HttpServletRequest doingPUT(HttpServletRequest request){
        when(request.getMethod()).thenReturn("PUT");
        return request;
    }

    private HttpServletRequest doingGET(HttpServletRequest request) {
        when(request.getMethod()).thenReturn("GET");
        return request;
    }

    /**
     * Fixme: unimplemented.
     * @param request
     * @param etag
     * @return
     */
    private HttpServletRequest clientWithCachedVersion(HttpServletRequest request,  String etag){
        return request;
    }

    private void verifyRequestNotPassedToApplication(FilterChain filterChainMock, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        verify(filterChainMock, never()).doFilter(request, response);
    }

    private void verifyEtagSet(HttpServletResponse responseMock, String value){
        verify(responseMock, times(1)).addHeader(ETAG_RESPONSE_HEADER, value);
    }

    private void havingCachingFilterInitialized() throws ServletException {
        CachingFilter filter = new CachingFilter();
        filter.init(null);
    }

    @Before
    public void createFilter() {
        filter = new CachingFilter();
    }

    @After
    public void disposeCachingFilter() {
        if (filter != null) {
            filter.destroy();
        }
    }

}