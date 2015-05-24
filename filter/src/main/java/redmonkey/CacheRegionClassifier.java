package redmonkey;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by urwisy on 2015-03-17.
 */
public interface CacheRegionClassifier {
    Region forRequest(HttpServletRequest request);
}
