package ai.latta.spring.wrappers;

import ai.latta.core.utilities.LogCapture;
import org.springframework.web.util.ContentCachingRequestWrapper;

public class RequestContextHolder {
    private static final ThreadLocal<ContentCachingRequestWrapper> requestHolder = new ThreadLocal<>();
    private static final ThreadLocal<LogCapture.LogCaptureEntry> logCaptureEntry = new ThreadLocal<>();

    public static void setRequest(ContentCachingRequestWrapper request) {
        requestHolder.set(request);
    }
    public static ContentCachingRequestWrapper getRequest() {
        return requestHolder.get();
    }
    public static void clear() {
        requestHolder.remove(); logCaptureEntry.remove();
    }

    public static void setLogCaptureEntry(LogCapture.LogCaptureEntry newLogCaptureEntry) { logCaptureEntry.set(newLogCaptureEntry); }
    public static LogCapture.LogCaptureEntry getLogCaptureEntry() { return logCaptureEntry.get(); }
}