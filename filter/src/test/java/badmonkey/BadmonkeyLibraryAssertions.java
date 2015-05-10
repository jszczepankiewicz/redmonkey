package badmonkey;

import badmonkey.cache.*;
import badmonkey.cache.CacheRegionAssert;
import org.assertj.core.api.Assertions;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by urwisy on 2015-03-26.
 */
public class BadmonkeyLibraryAssertions extends Assertions {

    /**
     * An entry point for CacheRegionAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myCacheRegion)</code> and get specific assertion with code completion.
     * @param actual the CacheRegion we want to make assertions on.
     * @return a new <code>{@link CacheRegionAssert}</code>
     */
    public static CacheRegionAssert assertThat(CacheRegion actual) {
        return new CacheRegionAssert(actual);
    }

    /**
     * An entry point for JedisPoolConfigAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myJedisPoolConfig)</code> and get specific assertion with code completion.
     * @param actual the JedisPoolConfig we want to make assertions on.
     * @return a new <code>{@link JedisPoolConfigAssert}</code>
     */
    public static JedisPoolConfigAssert assertThat(JedisPoolConfig actual) {
        return new JedisPoolConfigAssert(actual);
    }

    /**
     * An entry point for EntryAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myEntry)</code> and get specific assertion with code completion.
     * @param actual the Entry we want to make assertions on.
     * @return a new <code>{@link EntryAssert}</code>
     */
    public static EntryAssert assertThat(Entry actual) {
        return new EntryAssert(actual);
    }

    /**
     * An entry point for CacheResultAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
     * With a static import, one can write directly: <code>assertThat(myEntry)</code> and get specific assertion with code completion.
     * @param actual the CacheResult we want to make assertions on.
     * @return a new <code>{@link CacheResultAssert}</code>
     */
    public static CacheResultAssert assertThat(CacheResult actual) {
        return new CacheResultAssert(actual);
    }
}
