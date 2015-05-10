package badmonkey;

import badmonkey.cache.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static badmonkey.cache.CacheRegion.Cacheability.PASSTHROUGH;
import static badmonkey.cache.ETag.SIZEOF_ETAG;
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * TODO: add response content encoding
 * @author jszczepankiewicz
 * @since 2015-04-14
 */
public class CachingFilter implements Filter{

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

        if("GET".equalsIgnoreCase(request.getMethod())) {
            CacheRegion cacheRegion = policy.getfor(request);

            if(cacheRegion.getCacheability() == PASSTHROUGH){
                chain.doFilter(req,res);
                return;
            }

            String key = cacheRegion.getKeyStrategy().keyFor(request);
            String requestEtag = ETag.get(request);
            CacheResult result = cache.fetchIfChanged(key, requestEtag);

            if(result.isUpsertNeeded()){
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                CachedResponseWrapper wrappedResponse = new CachedResponseWrapper(response, baos);
                //  invoking "production" of content from underlying resources
                chain.doFilter(req, wrappedResponse);
                //  caching response for future use
                String generated = baos.toString("UTF-8");
                LOG.debug("generated content of size: {}", generated.length());
                cache.upsert(key, generated, ETag.of(generated, new StringBuilder(SIZEOF_ETAG)), cacheRegion.getTtl(), cacheRegion.getTtlUnit());
                //  now we need to copy from generated stream into original stream
                res.getOutputStream().write(baos.toByteArray());
                res.getOutputStream().flush();
                return;
            }
            else{
                if(result.getStoredEtag() == null){
                    //  client already has latest version
                    response.setStatus(SC_NOT_MODIFIED);
                    return;
                }
                else{
                    //  client has old version, we need to sent him latest one
                    response.setStatus(SC_OK);
                    ETag.set(response, result.getStoredEtag());
                    res.getOutputStream().write(result.getPayload().getBytes());
                    res.getOutputStream().flush();
                }
            }
        }
        else{
            //  passthrough anything else than GET without checking & saving in cache
            chain.doFilter(req,res);
            return;
        }
    }

    @Override
    public void destroy() {
        //TODO: cache repository shutdown
    }
}