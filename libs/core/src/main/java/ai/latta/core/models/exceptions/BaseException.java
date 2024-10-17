package ai.latta.core.models.exceptions;

import ai.latta.core.models.SystemInfo;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Map;

public abstract class BaseException {

    protected BaseException(String type) {
        this.type = type;
    }

    public enum Level {
        ERROR, FATAL, WARN
    }

    public Date timestamp;
    public Level level;
    public String name;
    public String message;
    public String stack;
    @JsonProperty("environment_variables")
    public Map<String, Object> environmentVariables;

    @JsonProperty("system_info")
    public SystemInfo systemInfo;
    public final String type;
}