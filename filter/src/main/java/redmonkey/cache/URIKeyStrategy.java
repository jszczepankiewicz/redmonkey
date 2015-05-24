package redmonkey.cache;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jszczepankiewicz
 * @since 2015-04-17
 */
public class URIKeyStrategy implements KeyStrategy{

    @Override
    public String keyFor(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @Override
    public int hashCode() {
        return this.getClass().toGenericString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }
}
