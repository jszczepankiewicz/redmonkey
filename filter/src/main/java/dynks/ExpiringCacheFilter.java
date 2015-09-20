package dynks;

import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by jszczepankiewicz on 2015-03-16.
 */
public class ExpiringCacheFilter implements Filter {

    private static final Logger LOG = getLogger(ExpiringCacheFilter.class);

    private JedisPool pool;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        try {
            LOG.debug("Initializing caching filter...");
            String host = "192.168.56.101";
            int port = 6379;
            pool = new JedisPool(PoolConfigurator.configure(filterConfig), host, port);
            LOG.info("Created connection pool to redis at {}:{}", host, port);
            //  we can get here even without redis being live, there is lazy connection creation in jedis connection pool
        }
        catch(Exception e){
            throw new ServletException("ExpiringCacheFilter initialization failed due to unexpected exception", e);
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if("GET".equalsIgnoreCase(request.getMethod())) {
            String uri = request.getRequestURI();

            try (Jedis jedis = pool.getResource()) {

                LOG.debug("Going into cache against: {}", uri);
                String content = jedis.get(uri);

                if(content == null){
                    LOG.info("Content not found, will be generated");
                    ByteArrayOutputStream baos =
                            new ByteArrayOutputStream();
                    CachedResponseWrapper wrappedResponse =
                            new CachedResponseWrapper(response, baos);
                    //  invoking "production" of content from underlying resources
                    chain.doFilter(req, wrappedResponse);
                    //  caching response for future use
                    String generated = baos.toString("UTF-8");
                    LOG.debug("generated content of size: {}", generated.length());
                    jedis.set(uri, baos.toString("UTF-8"));
                    jedis.expire(uri, 20);

                    //  now we need to copy from generated stream into original stream
                    res.getOutputStream().write(baos.toByteArray());
                    res.getOutputStream().flush();
                    return;
                }
                else{
                    LOG.debug("Content found");
                    res.getOutputStream().write(content.getBytes());
                    LOG.debug("Response written of size: {}", content.length());
                    res.getOutputStream().flush();
                    return;
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
        LOG.info("Shutting down redis connection pool...");

        if(pool != null){
            pool.destroy();
        }
    }
}
