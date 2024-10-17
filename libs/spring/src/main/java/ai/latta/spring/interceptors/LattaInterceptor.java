package ai.latta.spring.interceptors;

import ai.latta.core.LattaClient;
import ai.latta.core.models.SystemInfo;
import ai.latta.core.models.api.CreateInstance;
import ai.latta.core.models.api.CreateSnapshot;
import ai.latta.core.models.api.Instance;
import ai.latta.core.models.exceptions.*;

import ai.latta.core.utilities.LogCapture;
import ai.latta.spring.utilities.ScriptResponseModify;
import ai.latta.spring.wrappers.RequestContextHolder;
import ai.latta.spring.wrappers.CapturingResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
public class LattaInterceptor implements HandlerInterceptor {
    private final LattaClient client;
    private LogCapture logCapture = new LogCapture();

    private volatile Instance instance = null;
    private Map<String, LogCapture.LogCaptureEntry> captureEntries = new HashMap<>();

    private final static List<String> AllowedResponseParseBodyTypes = Arrays.asList("text/plain", "text/html", "application/json");

    public LattaInterceptor(String apiKey) {
        client = new LattaClient(apiKey);

        client.createInstance(new CreateInstance("spring", "TODO")).thenAccept(e -> {
           this.instance = e;
        });
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var logCaptureEntry = this.logCapture.addCaptureEntry();
        captureEntries.put(request.getRequestId(), logCaptureEntry);

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        RequestContextHolder.setRequest(wrappedRequest);
        RequestContextHolder.setLogCaptureEntry(logCaptureEntry);

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    private RequestException.Request constructRequestFromServletRequest() {
        ContentCachingRequestWrapper exRequest = RequestContextHolder.getRequest();

        var request = new RequestException.Request();
        var headers = new HashMap<String, String>();

        Enumeration<String> headerNames = exRequest.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                var headerName = headerNames.nextElement();
                headers.put(headerName, exRequest.getHeader(headerName));
            }
        }

        request.headers = headers;
        request.method = RequestException.HttpMethod.valueOf(exRequest.getMethod());
        request.url = exRequest.getRequestURL().toString();

        var query = new HashMap<String, String>();

        Enumeration<String> paramNames = exRequest.getParameterNames();
        while (paramNames.hasMoreElements()) {
            var paramName = paramNames.nextElement();
            query.put(paramName, exRequest.getParameter(paramName));
        }
        request.query = query;

        try {
            byte[] content = exRequest.getContentAsByteArray();
            request.body = new String(content, exRequest.getCharacterEncoding());
        }
        catch(UnsupportedEncodingException e) {}

        request.params = new HashMap<>();
        request.route = exRequest.getRequestURI();

        RequestContextHolder.clear();
        return request;
    }

    private RequestException.Response constructRequestFromServletResponse(HttpServletResponse exResponse) {

        RequestException.Response response = new RequestException.Response();

        response.statusCode = exResponse.getStatus();
        if (exResponse instanceof CapturingResponseWrapper) {
            CapturingResponseWrapper wrappedResponse = (CapturingResponseWrapper) exResponse;
            response.headers =  wrappedResponse.getCapturedHeaders();
            response.body = new String(wrappedResponse.getCapturedResponseBody());
        }
        return response;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);

        if (response instanceof CapturingResponseWrapper) {
            CapturingResponseWrapper wrappedResponse = (CapturingResponseWrapper) response;

            var modify = new ScriptResponseModify(client.getApiKey());
            modify.rewriteResponse(response, wrappedResponse);
        }

        if(ex == null || instance == null) {
            RequestContextHolder.clear();
            HandlerInterceptor.super.afterCompletion(request, response, handler, null);
            return;
        }

        var logCaptureEntry = RequestContextHolder.getLogCaptureEntry();
        var logEntries = logCapture.getEntriesBetween(logCaptureEntry.createdAt, new Date());

        RequestException requestException = new RequestException();
        requestException.request = constructRequestFromServletRequest();
        requestException.response = constructRequestFromServletResponse(response);
        requestException.environmentVariables =new HashMap<>(System.getenv());
        requestException.level = BaseException.Level.ERROR;
        requestException.name = ex.getClass().getSimpleName();

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();
        requestException.stack = exceptionAsString;

        requestException.systemInfo =new SystemInfo();
        requestException.message = ex.getMessage();
        requestException.timestamp = new Date();

        requestException.logs = new RequestException.LogEntries();
        requestException.logs.entries = logEntries;

        var relationHeader = requestException.request.headers.get("Latta-Relation-Id");
        var createSnapshotData = relationHeader != null ? CreateSnapshot.fromRelatedRelation("Message", relationHeader) : CreateSnapshot.fromRelation("Message");

        client.createSnapshot(instance, createSnapshotData).thenAccept(snapshot -> {
            client.attachRecord(snapshot, requestException);
        });

        logCapture.removeCaptureEntry(logCaptureEntry);
    }
}
