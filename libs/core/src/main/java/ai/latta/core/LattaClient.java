package ai.latta.core;

import ai.latta.core.models.AttachmentType;
import ai.latta.core.models.api.*;
import ai.latta.core.models.exceptions.BaseException;

import java.util.concurrent.CompletableFuture;

public class LattaClient {
    private final String apiKey;
    public final LattaAPI apiClient;

    public LattaClient(String apiKey) {
        var buildConfig = new BuildConfig();
        this.apiClient = new LattaAPI(buildConfig.getApiUrl(), apiKey);
        this.apiKey = apiKey;
    }

    public CompletableFuture<Instance> createInstance(CreateInstance data) {
        return this.apiClient.createInstance(data);
    }

    public CompletableFuture<Snapshot> createSnapshot(Instance instance, CreateSnapshot data) {
        return this.apiClient.createSnapshot(instance, data);
    }

    public CompletableFuture<Attachment> attachRecord(Snapshot snapshot, BaseException exception) {
        return this.apiClient.attachData(snapshot, AttachmentType.Record, exception);
    }

    public String getApiKey() {
        return apiKey;
    }
}
