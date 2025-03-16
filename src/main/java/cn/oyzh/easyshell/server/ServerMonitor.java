package cn.oyzh.easyshell.server;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ServerMonitor {

    private int ulimit;

    private String arch;

    private String uname;

    private double cpuUsage;

    private double memoryUsage;

    private double totalMemory;

    public int getUlimit() {
        return ulimit;
    }

    public void setUlimit(int ulimit) {
        this.ulimit = ulimit;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

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
