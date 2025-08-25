package cn.oyzh.easyshell.ssh2.server;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ShellServerMonitor {

    /**
     * cpu使用率
     */
    private double cpuUsage;

    /**
     * 内存使用率
     */
    private double memoryUsage;

    /**
     * 磁盘读取速度
     */
    private double diskReadSpeed;

    /**
     * 磁盘写入速度
     */
    private double diskWriteSpeed;

    /**
     * 网络发送速度
     */
    private double networkSendSpeed;

    /**
     * 网络接收速度
     */
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
