package ai.latta.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BuildConfig {
    private Properties properties;

    public BuildConfig() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("lib.properties")) {
            if (input == null) {
                return;
            }
            properties.load(input);
        }catch (IOException e) { }
    }

    public String getApiUrl() {
        return properties.getProperty("api.url");
    }
}
