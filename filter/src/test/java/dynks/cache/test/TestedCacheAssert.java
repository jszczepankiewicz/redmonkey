package dynks.cache.test;

import org.assertj.core.api.AbstractAssert;
import org.mockito.ArgumentCaptor;
import dynks.http.ETag;
import dynks.cache.TestValues;
import dynks.cache.TestedCache;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * @author jszczepankiewicz
 * @since 2015-08-01
 */
public class TestedCacheAssert extends AbstractAssert<TestedCacheAssert, TestedCache> {

    public TestedCacheAssert(TestedCache actual) {
        super(actual, TestedCacheAssert.class);
    }

    public static TestedCacheAssert assertThat(TestedCache actual) {
        return new TestedCacheAssert(actual);
    }

    public TestedCacheAssert servedResponseByGeneratingIt() {

        isNotNull();

        //  then
        try {
            verify(actual.getFilterChainMock(), times(1)).doFilter(actual.getRequest(), actual.getResponse());
        } catch (IOException | ServletException e) {
            throw new IllegalStateException("Unexpected IOException", e);
        }

        return this;

    }

    private byte[] getResponsePayload() throws IOException {

        if(actual.getResponseWritten() == null){
            ArgumentCaptor<byte[]> responseContentCaptor = ArgumentCaptor.forClass(byte[].class);
            verify(actual.getOutputStreamMock(), times(1)).write(responseContentCaptor.capture());
            actual.setResponseWritten(responseContentCaptor.getValue());
        }

        return actual.getResponseWritten();
    }

    private String getResponsePayloadAsString() throws IOException{
        return new String(getResponsePayload(), "UTF8");
    }

    public TestedCacheAssert hasStoredResponseInCache() throws IOException {

        isNotNull();

        //  String etag = ETag.of(generated, new StringBuilder(SIZEOF_ETAG));
        //  cache.upsert(key, generated, etag, res.getContentType(), cacheRegion.getTtl(), cacheRegion.getTtlUnit());
        String expectedKey = TestValues.TEST_NAMESPACE + ":" + actual.getRequest().getRequestURI();
        String payloadString = getResponsePayloadAsString();
        String expectedEtag = ETag.of(payloadString, new StringBuilder(ETag.SIZEOF_ETAG));


        verify(actual.getRepoMock(), times(1)).upsert(expectedKey, payloadString, expectedEtag, actual.getResponse().getContentType(), TestedCache.TEST_CACHE_TTL, TestedCache.TEST_CACHE_TTL_UNIT);
        return this;
    }

    public TestedCacheAssert hasNotStoredResponseInCache() {

        isNotNull();

        //  should not even touch the cache repository nor do something with reponse
        verifyZeroInteractions(actual.getRepoMock(), actual.getResponse());
        return this;
    }
}
