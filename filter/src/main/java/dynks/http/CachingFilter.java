package dynks.http;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import dynks.ProbeFactory.Probe;
import dynks.redis.RedisCacheRepositoryConfigBuilder;
import org.slf4j.Logger;
import dynks.cache.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static dynks.ProbeFactory.getProbe;
import static dynks.http.ETag.*;
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.slf4j.LoggerFactory.getLogger;
import static dynks.cache.CacheRegion.Cacheability.PASSTHROUGH;
import static dynks.http.HttpMethod.GET;

/**
 *
 * @author jszczepankiewicz
 * @since 2015-04-14
 */
public class CachingFilter implements Filter {

    private static final Logger LOG = getLogger(CachingFilter.class);

    private CacheRepository cache;
    private ResponseCacheByURIPolicy policy;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Config config = ConfigFactory.load("redmonkey");
        cache = RedisCacheRepositoryConfigBuilder.build(config);
        policy = ResponseCacheByURIBuilder.build(config);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;


        if (GET.equalsIgnoreCase(request.getMethod())) {

            Probe probe = getProbe(LOG);

            try {
                probe.log(request.getRequestURI());

                CacheRegion cacheRegion = policy.getfor(request);

                if (cacheRegion.getCacheability() == PASSTHROUGH) {
                    chain.doFilter(req, res);
                    probe.log("passthrough");
                    return;
                }

                String key = cacheRegion.getKeyStrategy().keyFor(request);
                String requestEtag = getFrom(request);
                probe.start('f');
                CacheQueryResult result = cache.fetchIfChanged(key, requestEtag);
                probe.stop();

                if (result.isUpsertNeeded()) {
                    probe.log("upsert");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    CachedResponseWrapper wrappedResponse = new CachedResponseWrapper(response, baos);
                    //  invoking "production" of content from underlying resources
                    chain.doFilter(req, wrappedResponse);
                    //  caching response for future use
                    String generated = baos.toString("UTF-8");
                    probe.log(baos.size());
                    String etag = of(generated, new StringBuilder(SIZEOF_ETAG));
                    probe.log(etag);
                    probe.log("upsert");
                    probe.start('u');
                    cache.upsert(key, generated, etag, res.getContentType(), cacheRegion.getTtl(), cacheRegion.getTtlUnit());
                    probe.stop();
                    writeIn(response, etag);
                    //  now we need to copy from generated stream into original stream
                    res.getOutputStream().write(baos.toByteArray());
                    res.getOutputStream().flush();

                    return;
                } else {
                    if (result.getStoredEtag() == null) {
                        //  client already has latest version
                        response.setStatus(SC_NOT_MODIFIED);
                        probe.log("not-changed");
                        return;
                    } else {
                        //  client has old version, we need to sent him latest one
                        response.setStatus(SC_OK);
                        writeIn(response, result.getStoredEtag());
                        res.setContentType(result.getContentType());
                        res.getOutputStream().write(result.getPayload().getBytes());
                        res.getOutputStream().flush();
                        probe.log("changed");
                    }
                }
            } finally {
                probe.flushLog();
            }
        } else {
            //  passthrough anything else than GET without checking & saving in cache
            chain.doFilter(req, res);
            return;
        }

    }

    private void doFilter(FilterChain chain, Probe probe, ServletRequest req, ServletResponse res) throws IOException, ServletException {
        probe.start('g');
        chain.doFilter(req, res);
        probe.stop();
    }

    @Override
    public void destroy() {

        if(cache!=null){
            cache.dispose();
        }
    }
}
