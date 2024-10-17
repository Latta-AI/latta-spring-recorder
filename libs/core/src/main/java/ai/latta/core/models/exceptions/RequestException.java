package ai.latta.core.models.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestException extends  BaseException {
    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
    }

    public static class LogEntries {
        public List<LogEntry> entries;
    }

    public static class Request {
        public HttpMethod method;
        public String url;
        public String route;
        public HashMap<String, String> params;
        public HashMap<String, String> query;
        public Map<String, String> headers;
        public Object body;
    }

    public static class Response {
        @JsonProperty("status_code")
        public int statusCode;
        public Map<String, String> headers;
        public Object body;
    }

    public static class LogEntry {
        public Date timestamp;
        public String level;
        public String message;
    }


    public Request request;
    public Response response;
    public LogEntries logs;

    public RequestException() {
        super("request");
    }
}


