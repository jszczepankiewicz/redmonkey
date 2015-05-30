package redmonkey.cache;

/**
 * Created by jszczepankiewicz on 2015-04-01.
 */
public class CacheResult {

    private final boolean upsertNeeded;
    private final String payload;
    private final String storedEtag;
    private final String contentType;

    public CacheResult(boolean upsertNeeded, String payload, String storedEtag, String contentType) {
        this.upsertNeeded = upsertNeeded;
        this.payload = payload;
        this.storedEtag = storedEtag;
        this.contentType = contentType;
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

    public String getContentType() {
        return contentType;
    }
}
