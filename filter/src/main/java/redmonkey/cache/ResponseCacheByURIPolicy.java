package redmonkey.cache;

import redmonkey.PatternedUrl;
import redmonkey.cache.CacheRegion.Cacheability;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableMap;
import static redmonkey.cache.NamespacedURIKeyStrategy.keyStrategyWithEmptyNamespace;

/**
 * Created by jszczepankiewicz on 2015-03-26.
 */
public class ResponseCacheByURIPolicy implements ResponseCachePolicy {

    private final Map<PatternedUrl, CacheRegion> regions;
    private final Set<PatternedUrl> uris;

    public static final CacheRegion PASSTHROUGH;

    static{
        PASSTHROUGH = new CacheRegion(0, null, Cacheability.PASSTHROUGH, keyStrategyWithEmptyNamespace());
    }

    public ResponseCacheByURIPolicy(Map<PatternedUrl, CacheRegion> regions) {

        if (regions == null) {
            throw new NullPointerException("List of cache regions should not be null");
        }

        this.regions = unmodifiableMap(regions);
        this.uris = regions.keySet();
    }

    @Override
    public CacheRegion getfor(final HttpServletRequest request) {

        final String requestURI = request.getRequestURI();

        for (PatternedUrl matcher : uris) {
            if (matcher.matches(requestURI)) {
                return regions.get(matcher);
            }
        }

        //  matching not found, assuming no caching for given request
        return PASSTHROUGH;
    }

    public Map<PatternedUrl, CacheRegion> getRegions() {
        return regions;
    }
}
