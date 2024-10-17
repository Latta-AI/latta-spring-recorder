package ai.latta.core.models;

import java.lang.management.ManagementFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.management.OperatingSystemMXBean;

public class SystemInfo {
    @JsonProperty("free_memory")
    public long freeMemory;

    @JsonProperty("total_memory")
    public long totalMemory;

    @JsonProperty("cpu_usage")
    public double cpuUsage;

    public SystemInfo() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        cpuUsage = osBean.getSystemCpuLoad();
        totalMemory = osBean.getTotalPhysicalMemorySize();
        freeMemory = osBean.getFreePhysicalMemorySize();
    }
}
