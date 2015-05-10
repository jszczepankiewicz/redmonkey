package badmonkey.cache;

import com.typesafe.config.ConfigFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static badmonkey.BadmonkeyLibraryAssertions.assertThat;
import static badmonkey.cache.ETag.SIZEOF_ETAG;
import static badmonkey.cache.Entry.ENTRY_ETAG;
import static badmonkey.cache.Entry.ENTRY_VALUE;
import static org.assertj.core.data.MapEntry.entry;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Integration test that will assume that redis server for testing will be run on localhost on default port.
 * Every execution will do the cleanup on before test.
 */
public class RedisCacheRepositoryTest {

    private static final Logger LOG = getLogger(RedisCacheRepositoryTest.class);

    private static final String JSON_SAVED = "{\"yourName\":\"alice\"}";
    private static final String KEY = "someKeyValue";
    private RedisCacheRepository repo;
    private StringBuilder etagBuilder = new StringBuilder(SIZEOF_ETAG);


    @Before
    public void cleanUpRedis() {
        repo = RedisCacheRepositoryConfigBuilder.build(ConfigFactory.load());

        //  clean up potential hashes
        try {
            getJedis().hdel(KEY, ENTRY_ETAG, ENTRY_VALUE);
        } catch (JedisConnectionException e) {
            LOG.error("connection exception while preparing integration test. Are you sure there is redis listening on default port on localhost?, details: ", e);
            throw e;
        }
    }

    @Test
    public void upsertValueIfNotExist() {

        //  given
        String etag = ETag.of(JSON_SAVED, etagBuilder);

        //  when
        repo.upsert(KEY, JSON_SAVED, etag, 999, TimeUnit.HOURS);

        //  then
        assertValueExist(KEY, etag, JSON_SAVED);

    }

    @Test
    public void upsertValueEvenIfKeyExistsWithDifferentEtag() {

        //  given
        havingEntryCached(KEY, JSON_SAVED, "someetagxyz");
        final String newContent = "[]";
        final String newEtag = ETag.of(newContent, etagBuilder);

        //  when
        repo.upsert(KEY, newContent, newEtag, 999, TimeUnit.HOURS);

        //  then
        assertValueExist(KEY, newEtag, newContent);
    }


    /*
     * there is no value for given key, CacheResult will return: upsertNeeded: true, payload: null, storedEtag: null
     */
    @Test
    public void detectMissingEntryOnFetch() {

        //  given
        final String key = "sk1";
        final String etag = "se1";

        //  when
        CacheResult result = repo.fetchIfChanged(key, etag);

        //  then
        assertThat(result).hasPayload(null).hasStoredEtag(null).isUpsertNeeded();
    }

    /*
     * there is value for given key but etag == null, CacheResult will return: upsertNeeded: false, payload contains the value,
     * storedEtag: corresponding etag value associated with given key
     */
    @Test
    public void returnEntryFromCacheWhenEtagUnknown() {

        //  given
        String etagExisting = ETag.of(JSON_SAVED, etagBuilder);
        havingEntryCached(KEY, JSON_SAVED, etagExisting);

        //  when
        CacheResult result = repo.fetchIfChanged(KEY, null);

        //  then
        assertThat(result).hasPayload(JSON_SAVED).hasStoredEtag(etagExisting).isUpsertNotNeeded();
    }

    /*
     *  there is value for given key and etag == storedEtag for given value, CacheResult will return: upsertNeeded: false,
     *  payload contain null, storedEtag: null
     */
    @Test
    public void fetchNotNeededAsCachedVersionNotChanged() {

        //  given
        String etagExisting = ETag.of(JSON_SAVED, etagBuilder);
        havingEntryCached(KEY, JSON_SAVED, etagExisting);

        //  when
        CacheResult result = repo.fetchIfChanged(KEY, etagExisting);

        //  then
        assertThat(result).hasPayload(null).hasStoredEtag(null).isUpsertNotNeeded();
    }

    /*
     * there is value for given key and etag != storedEtag for given value, CacheResult will return: upsertNeeded: false,
     * payload contain latest version, storedEtag: etag corresponding with given value
     */
    @Test
    public void fetchReturnedNewerEntry() {

        //  given
        String etagExisting = ETag.of(JSON_SAVED, etagBuilder);
        havingEntryCached(KEY, JSON_SAVED, etagExisting);

        //  when
        CacheResult result = repo.fetchIfChanged(KEY, "someolderetag");

        //  then
        assertThat(result).hasPayload(JSON_SAVED).hasStoredEtag(etagExisting).isUpsertNotNeeded();
    }


    @Ignore
    @Test
    public void removeKeyIfExist(){

    }

    //  test utils

    private Jedis getJedis() {
        return new Jedis("localhost");
    }

    private void havingEntryCached(String key, String content, String etag) {
        Jedis jedis = getJedis();
        jedis.hmset(key, new Entry(content, etag));
    }

    private void assertValueExist(String key, String expectedEtag, String expectedContent) {
        Jedis jedis = getJedis();

        long start = System.nanoTime();
        Map<String, String> out = jedis.hgetAll(key);
        System.out.println("hash get took (ms): " + (System.nanoTime() - start) / 1000000);

        assertThat(out).isNotEmpty()
                .hasSize(2)
                .contains(entry(ENTRY_VALUE, expectedContent))
                .contains(entry(ENTRY_ETAG, expectedEtag));
    }

}