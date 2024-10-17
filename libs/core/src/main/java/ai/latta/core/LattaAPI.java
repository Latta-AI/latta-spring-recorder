package ai.latta.core;

import ai.latta.core.models.api.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LattaAPI {
    private final String apiRoute;
    private final String apiKey;
    private final HttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LattaAPI(String apiRoute, String apiKey) {
        var builder = HttpClient.newBuilder();

        this.client = builder.build();
        this.apiKey = apiKey;
        this.apiRoute = apiRoute;
    }

    /**
     * Build new request
     * @return
     */
    private HttpRequest.Builder buildRequest() {
        return HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + apiKey);
    }

    /**
     * Fetches data from API and returns deserialized object of type TResult
     * @param route The API route to call
     * @param method HTTP method to use (GET, POST, etc.)
     * @param data Request body data (as JSON string)
     * @param responseType The class type of the response (used for deserialization)
     * @param <TResult> The type of the expected response object
     * @return A CompletableFuture that resolves to the TResult object
     */
    protected <TResult, TRequest> CompletableFuture<TResult> fetch(String route, String method, TRequest data, Class<TResult> responseType) {
        String requestBody;
        try {
            // Serialize the request body to JSON
            requestBody = objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return CompletableFuture.completedFuture(null);
            // throw new RuntimeException("Failed to serialize request body", e);
        }

        var request = buildRequest()
                .uri(URI.create(apiRoute + route))
                .method(method, HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .build();

        // Send the request asynchronously
        return this.client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .handle((response, throwable) -> {
                    if (throwable != null) {
                        // Return null on sendAsync error
                        return null;
                    }
                    try {
                        var body = response.body();
                        // Deserialize the response body to TResult
                        return objectMapper.readValue(body, responseType);
                    } catch (Exception e) {
                        // Return null on deserialization error
                        return null;
                    }
                });
    }

    /**
     * Fetches data from API with multipart/form-data for file uploads.
     * @param route The API route to call
     * @param method HTTP method to use (GET, POST, etc.)
     * @param data Map containing fields and file objects to be uploaded
     * @param responseType The class type of the response (used for deserialization)
     * @param <TResult> The type of the expected response object
     * @return A CompletableFuture that resolves to the TResult object
     */
    protected  <TResult> CompletableFuture<TResult> multipartFetch(
            String route,
            String method,
            Map<String, Object> data,
            Class<TResult> responseType) {

        String boundary = "Boundary-" + System.currentTimeMillis();
        var requestBody = buildMultipartBody(data, boundary);

        var request = buildRequest()
                .uri(URI.create(apiRoute + route))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .method(method, HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();

        // Send the request asynchronously
        return this.client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        // Deserialize the response body to TResult
                        return objectMapper.readValue(body, responseType);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize response", e);
                    }
                });
    }

    /**
     * Build the multipart body from fields and files.
     *
     * @param data Map containing fields and file objects
     * @param boundary The boundary string for multipart form
     * @return The byte array representing the multipart form body
     */
    private byte[] buildMultipartBody(Map<String, Object> data, String boundary) {
        StringBuilder body = new StringBuilder();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            body.append("--").append(boundary).append("\r\n");
            if (entry.getValue() instanceof File) {
                File file = (File) entry.getValue();
                body.append("Content-Disposition: form-data; name=\"").append(entry.getKey())
                        .append("\"; filename=\"").append(file.getName()).append("\"\r\n");
                body.append("Content-Type: application/octet-stream\r\n\r\n");
                body.append(new String(readFileBytes(file))).append("\r\n"); // Read file bytes
            } else {
                // Assuming it's a string for form fields
                body.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n\r\n");
                body.append(entry.getValue()).append("\r\n");
            }
        }
        body.append("--").append(boundary).append("--\r\n"); // End of multipart

        return body.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Read the file bytes to be uploaded.
     *
     * @param file The file to read
     * @return The byte array of the file
     */
    private byte[] readFileBytes(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + file.getName(), e);
        }
    }


    /**
     * Create new instance
     * @param data Instance data
     * @return Instance or null
     */
    public CompletableFuture<Instance> createInstance(CreateInstance data) {
        return this.fetch("/instance/backend", "PUT", data, Instance.class);
    }

    /**
     * Create new snapshot
     * @param instance Target instance
     * @param data Snapshot data
     * @return Snapshot or null
     */
    public CompletableFuture<Snapshot> createSnapshot(Instance instance, CreateSnapshot data) {
        return this.fetch("/snapshot/" + instance.id, "PUT", data, Snapshot.class);
    }

    /**
     * Attach data to snapshot
     * @param snapshot Target snapshot
     * @param type Attachment type
     * @param data Attachment data
     * @return
     */
    public CompletableFuture<Attachment> attachData(Snapshot snapshot, String type, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("data", data);

        return this.fetch("/snapshot/" + snapshot.id + "/attachment", "PUT", map, Attachment.class);
    }
}
