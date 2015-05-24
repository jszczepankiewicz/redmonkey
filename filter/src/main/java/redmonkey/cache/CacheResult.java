package redmonkey.cache;

/**
 * Created by urwisy on 2015-04-01.
 */
public class CacheResult {

    private final boolean upsertNeeded;
    private final String payload;
    private final String storedEtag;

    public CacheResult(boolean upsertNeeded, String payload, String storedEtag) {
        this.upsertNeeded = upsertNeeded;
        this.payload = payload;
        this.storedEtag = storedEtag;
    }

    public boolean isUpsertNeeded() {
        return upsertNeeded;
    }

    public String getPayload() {
        return payload;
    }

    public String getStoredEtag() {
        return storedEtag;
    }
}
