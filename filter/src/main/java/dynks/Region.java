package dynks;

/**
 * Created by jszczepankiewicz on 2015-03-17.
 */
public interface Region {

    enum Volatility{
        CACHED,
        NON_CACHED
    }

    Volatility getVolatility();
    int getTTLSeconds();
    String getKey();
}
