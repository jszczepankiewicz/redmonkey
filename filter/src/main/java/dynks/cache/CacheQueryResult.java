package dynks.cache;

/**
 * Extends CachedContent by adding volatile (non-cached in redis) instruction about cache invalidation.
 * Created by jszczepankiewicz on 2015-04-01.
 */
public class CacheQueryResult extends CachedContent {

    private final boolean upsertNeeded;

    public CacheQueryResult(boolean upsertNeeded, String payload, String storedEtag, String contentType) {
        super(storedEtag, payload, contentType);
        this.upsertNeeded = upsertNeeded;
    }

    public boolean isUpsertNeeded() {
        return upsertNeeded;
    }

}
