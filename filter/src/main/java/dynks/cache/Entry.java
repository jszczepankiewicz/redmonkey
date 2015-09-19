package dynks.cache;

import java.util.HashMap;

/**
 * Cache content extending directly from HashMap to being able to be provided directly without conversion to jedis.
 */
public class Entry extends HashMap<String, String>{

    public static final String ENTRY_VALUE = "v";
    public static final String ENTRY_ETAG = "e";
    public static final String ENTRY_CONTENT_TYPE = "c";

    public Entry(String content, String etag, String contentType){

        super(3);

        if(content == null){
            throw new NullPointerException("Content to put into cache should not be null");
        }

        if(etag == null){
            throw new NullPointerException("Etag to put into cache should not be null");
        }

        if(contentType == null){
            throw new NullPointerException("Content type should not be null");
        }

        this.put(ENTRY_ETAG, etag);
        this.put(ENTRY_VALUE, content);
        this.put(ENTRY_CONTENT_TYPE, contentType);
    }

    public String getContent(){
        return get(ENTRY_VALUE);
    }

    public String getEtag(){
        return get(ENTRY_ETAG);
    }

    public String getContentType() {
        return get(ENTRY_CONTENT_TYPE);
    }
}
