package badmonkey.cache;

import badmonkey.PatternedUrl;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Builder for creation of ResponseCacheByURIPolicy object that is using configuration in file stored in classpath.
 */
public class ResponseCacheByURIBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseCacheByURIBuilder.class);

    public static ResponseCacheByURIPolicy build(Config config) {

        List<? extends Config> configuredRegions = config.getConfigList("redmonkey.regions");
        Map<PatternedUrl, CacheRegion> regions = new HashMap<>(configuredRegions.size());
        URIKeyStrategy keyStrategy = new URIKeyStrategy();
        for (Config region : configuredRegions) {
            //  for now only URIKeyStrategy supported
            CacheRegion cached = new CacheRegion(region.getDuration("ttl", MILLISECONDS), MILLISECONDS,keyStrategy);
            String url = region.getString("pattern");
            regions.put(new PatternedUrl(url), cached);
            LOG.debug("Loaded cached region against: {} with ttl: {} {}", url, cached.getTtl(), cached.getTtlUnit());
        }

        LOG.info("Configured {} cached URL regions", configuredRegions.size());

        return new ResponseCacheByURIPolicy(regions);
    }
}
