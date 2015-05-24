package redmonkey.cache;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static redmonkey.BadmonkeyLibraryAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jszczepankiewicz
 * @since 2015-04-17
 */
public class URIKeyStrategyTest {

    @Test
    public void twoInstancesShouldBeEqual(){

        //  given
        URIKeyStrategy keyStrategy1 = new URIKeyStrategy();
        URIKeyStrategy keyStrategy2 = new URIKeyStrategy();

        //  when
        int hashCode1 = keyStrategy1.hashCode();
        int hashCode2 = keyStrategy2.hashCode();

        //  then
        assertThat(hashCode1).isEqualTo(hashCode2);
        assertThat(keyStrategy1).isEqualTo(keyStrategy2);
    }

    @Test
    public void generatedKeyShouldBeEqualToRequestURI(){

        //  given
        final String uri = "/v1/superduper/xyz";
        final HttpServletRequest req = forURI(uri);
        final KeyStrategy keyStrategy = new URIKeyStrategy();

        //  when
        String key = keyStrategy.keyFor(req);

        //  then
        assertThat(key).isEqualTo(uri);
    }

    private HttpServletRequest forURI(final String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }

}