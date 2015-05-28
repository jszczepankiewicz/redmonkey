package redmonkey.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by jszczepankiewicz on 2015-04-01.
 */
public class RedisCacheRepository implements CacheRepository {

    private final JedisPool pool;


    private final JedisPoolConfig poolConfig;
    private final String host;
    private final int port;

    public static final CacheResult NO_RESULT_FOUND = new CacheResult(true, null, null);
    private static final CacheResult RESULT_FOUND_BUT_NOT_CHANGED = new CacheResult(false, null, null);

    public RedisCacheRepository(JedisPoolConfig poolConfig, String host, int port){

        this.host = host;
        this.port = port;
        this.poolConfig = poolConfig;
        this.pool = new JedisPool(poolConfig, host, port);
    }

    /**
     * Return entry assuming exist. If not then it reacts as it would not exist.
     * @param key
     * @return
     */
    private CacheResult getEntryAssumingCached(Jedis jedis, String key){

        Map<String, String> out = jedis.hgetAll(key);

        if(out == null){
            return NO_RESULT_FOUND;
        }

        return new CacheResult(false, out.get(Entry.ENTRY_VALUE), out.get(Entry.ENTRY_ETAG));
    }

    @Override
    public CacheResult fetchIfChanged(String key, String etag) {

        try (Jedis jedis = pool.getResource()) {

            //  client does not have any version, query for both content + etag
            if(etag == null){
                return getEntryAssumingCached(jedis, key);
            }

            /*
                get value of etag assuming key exists. This is less costly as checking if key exists and get
             */
            String cachedEtag = jedis.hget(key, Entry.ENTRY_ETAG);

            if(cachedEtag == null){
                return NO_RESULT_FOUND;
            }

            if(cachedEtag.equals(etag)){
                return RESULT_FOUND_BUT_NOT_CHANGED;
            }

            /*
              entry in cache different, we assume cached entry is newer than on client side
              we need also to take into consideration that durint last check above entry expired
              thus may not exist when queried for full content.
             */
            return getEntryAssumingCached(jedis, key);
        }
    }

    @Override
    public void upsert(String key, String content, String etag, long ttl, TimeUnit ttlUnit) {

        long start = System.nanoTime();

        try (Jedis jedis = pool.getResource()) {
            //  todo refactor to have multicommand?
            jedis.hmset(key, new Entry(content, etag));
            if(ttl>0){
                jedis.expire(key, (int)ttlUnit.toSeconds(ttl));
            }
        }

        System.out.println("upsert took (ms): " + (System.nanoTime() - start)/1_000_000);
    }

    @Override
    public void remove(String key) {

    }

    public JedisPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
