package redmonkey.cache;

/**
 * Created by urwisy on 2015-04-06.
 */
public class Strings {
    public static void recycle(StringBuilder builder){
        builder.delete(0, builder.length());
    }
}
