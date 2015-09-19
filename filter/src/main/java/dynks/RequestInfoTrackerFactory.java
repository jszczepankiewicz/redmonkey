package dynks;

import org.slf4j.Logger;

/**
 * @author jszczepankiewicz
 * @since 2015-09-18
 */
public class RequestInfoTrackerFactory {

    private static final LogWriter NOP = new NOPLogWriter();

    public LogWriter build(Logger log){
        if(log.isDebugEnabled()){
            return new DebugLogWriter(log);
        }
        else{
            return NOP;
        }
    }

    interface LogWriter{

        /** starts tracking time for given category */
        void startTrackingTime(char category);
        /** stops tracking time */
        void stopTrackingTime();
        /** adds debug line*/
        void debug(String activity);
        /** prints logs*/
        void flushLog();
    }

    /**
     * Lazy b...d does nothing saving air on planet Earth.
     */
    static class NOPLogWriter implements LogWriter{
        @Override
        public void startTrackingTime(char category) {}

        @Override
        public void stopTrackingTime() {}

        @Override
        public void debug(String activity) {}

        @Override
        public void flushLog() {}
    }

    static class DebugLogWriter implements LogWriter{

        private StringBuilder activityBuffer;
        private StringBuilder trackingBuffer;

        private final Logger log;

        DebugLogWriter(Logger log){
            this.log = log;
        }

        @Override
        public void startTrackingTime(char category) {

        }

        @Override
        public void stopTrackingTime() {

        }

        @Override
        public void debug(String activity) {

        }

        @Override
        public void flushLog() {

        }
    }
}
