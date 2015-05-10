package badmonkey;

import sun.misc.Unsafe;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;

/**
 * Created by urwisy on 2015-03-28.
 */
public class Probes {

    public static Unsafe getUnsafe() {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new IllegalStateException("Exception while attempt to retrieve Unsafe", e);
        }
    }

    public static long sizeOf(Object object) {
        Unsafe unsafe = getUnsafe();
        return unsafe.getAddress(normalizeAddress(unsafe.getInt(object, 4L)) + 12L);
    }

    /**
     * We need to use normalizeAddress() function because addresses between 2^31 and 2^32 will be automatically converted
     * to negative integers, i.e. stored in complement form.
     *
     * @param value
     * @return
     */
    public static long normalizeAddress(int value) {
        if (value >= 0) return value;
        return (~0L >>> 32) & value;
    }

    public static class HeapMemory{

        private static MemoryMXBean mbean;
        private static long heapBytesUsedOnStart;
        private static long heapBytesUsedOnStop;

        public static void startCollecting(){

            if(mbean == null) {
                mbean = ManagementFactory.getMemoryMXBean();
            }

            heapBytesUsedOnStart = mbean.getHeapMemoryUsage().getUsed();
            heapBytesUsedOnStop = 0;
        }

        public static void stopCollecting(){
            heapBytesUsedOnStop = mbean.getHeapMemoryUsage().getUsed();
        }


        public static void stopCollectingAndPrint(){
            stopCollecting();
            System.out.println("Allocated " + (heapBytesUsedOnStop - heapBytesUsedOnStart) + " heap bytes");
        }
    }
}
