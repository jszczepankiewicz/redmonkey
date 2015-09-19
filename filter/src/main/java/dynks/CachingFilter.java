package dynks;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import dynks.cache.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.slf4j.LoggerFactory.getLogger;
import static dynks.cache.CacheRegion.Cacheability.PASSTHROUGH;
import static dynks.cache.ETag.SIZEOF_ETAG;
import static dynks.http.HttpMethod.GET;

/**
 * TODO: add response content encoding
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
            StringBuilder logbuff = new StringBuilder(600);

            try {
                logbuff.append(request.getRequestURI());
                logbuff.append("|");

                CacheRegion cacheRegion = policy.getfor(request);

                if (cacheRegion.getCacheability() == PASSTHROUGH) {
                    chain.doFilter(req, res);
                    logbuff.append("passthrough");
                    return;
                }

                String key = cacheRegion.getKeyStrategy().keyFor(request);
                String requestEtag = ETag.get(request);
                CacheQueryResult result = cache.fetchIfChanged(key, requestEtag);

                if (result.isUpsertNeeded()) {
                    logbuff.append("upsert|");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    CachedResponseWrapper wrappedResponse = new CachedResponseWrapper(response, baos);
                    //  invoking "production" of content from underlying resources
                    chain.doFilter(req, wrappedResponse);
                    //  caching response for future use
                    String generated = baos.toString("UTF-8");
                    logbuff.append(baos.size());
                    logbuff.append("b|");
                    String etag = ETag.of(generated, new StringBuilder(SIZEOF_ETAG));
                    logbuff.append(etag);
                    logbuff.append("|");
                    long start = System.nanoTime();
                    cache.upsert(key, generated, etag, res.getContentType(), cacheRegion.getTtl(), cacheRegion.getTtlUnit());
                    logbuff.append("upsert:");
                    logbuff.append((System.nanoTime() - start) / 1000);
                    logbuff.append("us|");
                    ETag.set(response, etag);
                    //  now we need to copy from generated stream into original stream
                    res.getOutputStream().write(baos.toByteArray());
                    res.getOutputStream().flush();

                    return;
                } else {
                    if (result.getStoredEtag() == null) {
                        //  client already has latest version
                        response.setStatus(SC_NOT_MODIFIED);
                        logbuff.append("not-changed");
                        return;
                    } else {
                        //  client has old version, we need to sent him latest one
                        response.setStatus(SC_OK);
                        ETag.set(response, result.getStoredEtag());
                        res.setContentType(result.getContentType());
                        res.getOutputStream().write(result.getPayload().getBytes());
                        res.getOutputStream().flush();
                        logbuff.append("changed");
                    }
                }
            } finally {
                LOG.debug(logbuff.toString());
            }
        } else {
            //  passthrough anything else than GET without checking & saving in cache
            chain.doFilter(req, res);
            return;
        }

    }

    @Override
    public void destroy() {
        //TODO: cache repository shutdown
    }
}