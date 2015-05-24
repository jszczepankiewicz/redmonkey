package redmonkey.cache;


import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static redmonkey.BadmonkeyLibraryAssertions.assertThat;
import static redmonkey.cache.ETag.ETAG_REQUEST_HEADER;
import static org.mockito.Mockito.*;

/**
 * @author jszczepankiewicz
 * @since 2015-04-17
 */
public class ETagTest {

    @Test
    public void shouldReadEtagFromRequest() {

        //  given
        final String etagHeader = "xyz";
        final HttpServletRequest request = forURIWithEtagHeader(etagHeader);

        //  when
        String etagFromRequest = ETag.get(request);

        //  then
        assertThat(etagFromRequest).isEqualTo(etagHeader);
    }

    @Test
    public void shouldSetEtagInResponse() {

        //  given
        HttpServletResponse response = responseMock();
        final String value = "someEtag";

        //  when
        ETag.set(response, value);

        //  then
        verify(response).addHeader(ETag.ETAG_RESPONSE_HEADER, value);
    }

    @Test
    public void shouldGenerateEtagFromValue() {

        //  given
        StringBuilder builder = new StringBuilder(ETag.SIZEOF_ETAG);
        String someValue1 = "{something1}";
        String someValue2 = "{something2}";

        //  when
        String etag1 = ETag.of(someValue1, builder);
        String etag2 = ETag.of(someValue2, builder);

        //  then
        assertThat(etag1).isNotEmpty();
        assertThat(etag2).isNotEmpty().isNotEqualTo(etag1);
    }

    private HttpServletResponse responseMock() {
        HttpServletResponse response = mock(HttpServletResponse.class);
        return response;
    }

    private HttpServletRequest forURIWithEtagHeader(final String etag) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(ETAG_REQUEST_HEADER)).thenReturn(etag);
        return request;
    }

}