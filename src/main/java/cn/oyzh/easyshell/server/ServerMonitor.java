package cn.oyzh.easyshell.server;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ServerMonitor {

    private double cpuUsage;

    private double memoryUsage;

    private double totalMemory;

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public double getTotalMemory() {
        return totalMemory;
    }

    public String getTotalMemoryInfo() {
        return totalMemory+"MB";
    }

    public void setTotalMemory(double totalMemory) {
        this.totalMemory = totalMemory;
    }
}
