package dynks;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by urwisy on 2015-03-17.
 */
public class CacheRegionByUrlClassifier implements CacheRegionClassifier{


    public CacheRegionByUrlClassifier(){
        //  here we should read some file & build region list

    }


    @Override
    public Region forRequest(HttpServletRequest request) {
        return null;
    }
}
