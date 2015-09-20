package dynks.cache;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import redis.clients.jedis.JedisPoolConfig;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by jszczepankiewicz on 2015-04-01.
 */
public class RedisCacheRepositoryConfigBuilder {

    private static final Logger LOG = getLogger(RedisCacheRepositoryConfigBuilder.class);

    public static RedisCacheRepository build(Config config) {

        String host = config.getString("redmonkey.redis.host");
        int port = config.getInt("redmonkey.redis.port");
        LOG.info("Connecting to redis at {}:{}", host, port);

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        final String testOnBorrow = "redmonkey.redis.pool.testConnectionOnBorrow";
        if(config.hasPath(testOnBorrow)) {
            poolConfig.setTestOnBorrow(config.getBoolean(testOnBorrow));
        }
        LOG.debug("\tTestOnBorrow: {}", poolConfig.getTestOnBorrow());

        final String testOnReturn = "redmonkey.redis.pool.testConnectionOnReturn";
        if(config.hasPath(testOnReturn)) {
            poolConfig.setTestOnReturn(config.getBoolean(testOnReturn));
        }
        LOG.debug("\tTestOnReturn: {}", poolConfig.getTestOnReturn());

        final String testWhileIdle = "redmonkey.redis.pool.testWhileIdle";
        if(config.hasPath(testWhileIdle)) {
            poolConfig.setTestWhileIdle(config.getBoolean(testWhileIdle));
        }
        LOG.debug("\tTestWhileIdle: {}", poolConfig.getTestWhileIdle());

        final String maxConnections = "redmonkey.redis.pool.maxTotalConnectionsToCache";
        if(config.hasPath(maxConnections)) {
            poolConfig.setMaxTotal(config.getInt(maxConnections));
        }
        LOG.debug("\tMaxTotal: {}", poolConfig.getMaxTotal());

        final String maxIdle = "redmonkey.redis.pool.maxIdle";
        if(config.hasPath(maxIdle)) {
            poolConfig.setMaxIdle(config.getInt(maxIdle));
        }
        LOG.debug("\tMaxIdle: {}", poolConfig.getMaxIdle());

        final String minIdle = "redmonkey.redis.pool.minIdle";
        if(config.hasPath(minIdle)) {
            poolConfig.setMinIdle(config.getInt(minIdle));
        }
        LOG.debug("\tMinIdle: {}", poolConfig.getMinIdle());

        final String numTestsPerEviction = "redmonkey.redis.pool.numberOfTestsPerEvictionRun";
        if(config.hasPath(numTestsPerEviction)) {
            poolConfig.setNumTestsPerEvictionRun(config.getInt(numTestsPerEviction));
        }
        LOG.debug("\tNumTestsPerEvictionRun: {}", poolConfig.getNumTestsPerEvictionRun());

        final String timeBetweenEviction = "redmonkey.redis.pool.msBetweenEvictionRuns";
        if(config.hasPath(timeBetweenEviction)) {
            poolConfig.setTimeBetweenEvictionRunsMillis(config.getInt(timeBetweenEviction));
        }
        LOG.debug("\tTimeBetweenEvictionRunsMillis: {}", poolConfig.getTimeBetweenEvictionRunsMillis());

        return new RedisCacheRepository(poolConfig, host, port);
    }
}
