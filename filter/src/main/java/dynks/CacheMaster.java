package dynks;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by urwisy on 2015-03-17.
 */
public class CacheMaster {

    private final CacheRegionClassifier classifier;

    public CacheMaster(CacheRegionClassifier classifier){
        this.classifier = classifier;
    }

    public Region forRequest(HttpServletRequest request){
        Region region = classifier.forRequest(request);
        return region;
    }

}
