package redmonkey.cache;

import java.util.HashMap;

/**
 * Cache content extending directly from HashMap to being able to be provided directly without conversion to jedis.
 */
public class Entry extends HashMap<String, String>{

    public static final String ENTRY_VALUE = "v";
    public static final String ENTRY_ETAG = "e";

    public Entry(String content, String etag){

        super(2);

        if(content == null){
            throw new NullPointerException("Content to put into cache should not be null");
        }

        if(etag == null){
            throw new NullPointerException("Etag to put into cache should not be null");
        }

        this.put(ENTRY_ETAG, etag);
        this.put(ENTRY_VALUE, content);
    }

    public String getContent(){
        return get(ENTRY_VALUE);
    }

    public String getEtag(){
        return get(ENTRY_ETAG);
    }

}
