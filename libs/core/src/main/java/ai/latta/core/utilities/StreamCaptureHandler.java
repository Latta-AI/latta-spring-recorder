package ai.latta.core.utilities;

import ai.latta.core.interfaces.StreamListener;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StreamCaptureHandler  extends PrintStream {
    private final PrintStream originalStream;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private StreamListener listener;

    public StreamCaptureHandler(PrintStream originalStream) {
        super(new ByteArrayOutputStream());
        this.originalStream = originalStream;
    }

    public void setListener(StreamListener listener) {
        this.listener = listener;
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        notifyListener(new String(buf, off, len));
        originalStream.write(buf, off, len);
    }

    @Override
    public void write(int b) {
        super.write(b);
        notifyListener(String.valueOf((char) b));
        originalStream.write(b);
    }

    private void notifyListener(String log) {
        if (listener != null) {
            listener.onLogCaptured(log);
        }
    }

    public void close() {
        System.setOut(originalStream);
        super.close();
    }

    public void startCapture() {
        System.setOut(this);
    }
}
