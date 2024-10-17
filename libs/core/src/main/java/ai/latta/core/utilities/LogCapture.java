package ai.latta.core.utilities;

import ai.latta.core.models.exceptions.RequestException;

import java.util.*;

public class LogCapture {
    private List<RequestException.LogEntry> entries = new ArrayList<>();
    private List<LogCaptureEntry> captureEntries = new ArrayList<>();

    private StreamCapture stdoutCapture;
    private StreamCapture stdErrCapture;

    public LogCapture() {
        stdoutCapture = new StreamCapture(System.out, this::onStdout);
        stdErrCapture = new StreamCapture(System.err, this::onStderr);
    }


    private void onStdout(String message) {
        var entry = new RequestException.LogEntry();
        entry.level = "INFO";
        entry.message = message;
        entry.timestamp = new Date();

        entries.add(entry);
    }

    private void onStderr(String message) {
        var entry = new RequestException.LogEntry();
        entry.level = "ERROR";
        entry.message = message;
        entry.timestamp = new Date();

        entries.add(entry);
    }


    public List<RequestException.LogEntry> getEntriesBetween(Date start, Date end) {
        List<RequestException.LogEntry> logs = new ArrayList<>();
        for(var capturedLog : entries) {
            if(capturedLog.timestamp.after(end)) break;
            if(capturedLog.timestamp.before(start)) continue;
            logs.add(capturedLog);
        }
        return logs;
    }

    private void eraseUnusedLogs() {
        LogCaptureEntry oldestEntry = captureEntries.stream()
                .min(Comparator.comparing(o -> o.createdAt))
                .orElse(null);

        if(oldestEntry == null) {return;}

        this.entries = List.of(this.entries.stream()
                .filter(x -> x.timestamp.after(oldestEntry.createdAt))
                .toArray(RequestException.LogEntry[]::new));
    }

    public LogCaptureEntry addCaptureEntry() {
        var entry = new LogCaptureEntry();
        entry.id = UUID.randomUUID().toString();
        entry.createdAt = new Date();
        return entry;
    }

    public void removeCaptureEntry(LogCaptureEntry entry) {
        this.captureEntries.remove(entry);
        eraseUnusedLogs();
    }

    public static class LogCaptureEntry {
        public String id;
        public Date createdAt;
    }

    public void close() {
        System.setOut(stdoutCapture.getSource());
        stdoutCapture.close();

        System.setErr(stdErrCapture.getSource());
        stdErrCapture.close();
    }
}
