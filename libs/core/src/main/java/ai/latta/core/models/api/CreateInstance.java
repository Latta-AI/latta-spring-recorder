package ai.latta.core.models.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.InetAddress;

public class CreateInstance {
    @JsonProperty("framework")
    String framework;

    @JsonProperty("framework_version")
    String frameworkVersion;

    @JsonProperty("os")
    String operatingSystem;

    @JsonProperty("device")
    String deviceName;

    @JsonProperty("lang")
    String language;

    public CreateInstance(String framework, String frameworkVersion) {
        this.framework = framework;
        this.frameworkVersion = frameworkVersion;

        this.operatingSystem = System.getProperty("os.name");
        try {
            this.deviceName = InetAddress.getLocalHost().getHostName();
        }catch (Exception e) {
            this.deviceName = "unknown";
        }

        // Hardcode this for now
        this.language = "en";
    }
}
