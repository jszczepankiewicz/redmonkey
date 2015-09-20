package dynks;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jszczepankiewicz on 2015-03-17.
 */
public interface CacheRegionClassifier {
    Region forRequest(HttpServletRequest request);
}
