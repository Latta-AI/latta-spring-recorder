package ai.latta.core.utilities;

import ai.latta.core.interfaces.StreamListener;
import java.io.PrintStream;
import java.util.Date;

class LogEntry {
    Date timestamp;
    String level;
    String message;
}

public class StreamCapture {
    private final PrintStream source;
    private final StreamCaptureHandler outputStream;

    public StreamCapture(PrintStream source, StreamListener listener) {
        this.source = source;
        outputStream = new StreamCaptureHandler(source);
        outputStream.setListener(listener);
    }

    PrintStream getSource() {
        return source;
    }

    PrintStream getInterceptedStream() {
        return outputStream;
    }

    public void close() {
        outputStream.close();
    }
}
