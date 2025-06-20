package cn.oyzh.easyshell.sshj.server;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ShellServerMonitor {

    private double cpuUsage;

    private double memoryUsage;

    private double diskReadSpeed;

    private double diskWriteSpeed;

    private double networkSendSpeed;

    private double networkReceiveSpeed;

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

}
