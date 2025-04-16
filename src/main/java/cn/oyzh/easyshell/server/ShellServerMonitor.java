package cn.oyzh.easyshell.server;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ShellServerMonitor {

//    private String ulimit;
//
//    private String arch;
//
//    private String uname;
//
//    private String uptime;

    private double cpuUsage;

    private double memoryUsage;

//    private double totalMemory;

    private double diskReadSpeed;

    private double diskWriteSpeed;

    private double networkSendSpeed;

    private double networkReceiveSpeed;

//    public String getUptime() {
//        return uptime;
//    }
//
//    public void setUptime(String uptime) {
//        this.uptime = uptime;
//    }

    public double getDiskReadSpeed() {
        return diskReadSpeed;
    }

    public void setDiskReadSpeed(double diskReadSpeed) {
        this.diskReadSpeed = diskReadSpeed;
    }

    public double getDiskWriteSpeed() {
        return diskWriteSpeed;
    }

    public void setDiskWriteSpeed(double diskWriteSpeed) {
        this.diskWriteSpeed = diskWriteSpeed;
    }

    public double getNetworkSendSpeed() {
        return networkSendSpeed;
    }

    public void setNetworkSendSpeed(double networkSendSpeed) {
        this.networkSendSpeed = networkSendSpeed;
    }

    public double getNetworkReceiveSpeed() {
        return networkReceiveSpeed;
    }

    public void setNetworkReceiveSpeed(double networkReceiveSpeed) {
        this.networkReceiveSpeed = networkReceiveSpeed;
    }

//    public String getUlimit() {
//        return ulimit;
//    }
//
//    public void setUlimit(String ulimit) {
//        this.ulimit = ulimit;
//    }
//
//    public String getArch() {
//        return arch;
//    }
//
//    public void setArch(String arch) {
//        this.arch = arch;
//    }
//
//    public String getUname() {
//        return uname;
//    }
//
//    public void setUname(String uname) {
//        this.uname = uname;
//    }

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

//    public double getTotalMemory() {
//        return totalMemory;
//    }
//
//    public String getTotalMemoryInfo() {
//        return totalMemory + "MB";
//    }
//
//    public void setTotalMemory(double totalMemory) {
//        this.totalMemory = totalMemory;
//    }
}
