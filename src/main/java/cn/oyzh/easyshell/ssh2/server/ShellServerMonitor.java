package cn.oyzh.easyshell.ssh2.server;

/**
 * 服务器监控信息
 *
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

    /**
     * 获取磁盘读取速度
     *
     * @return 磁盘读取速度
     */
    public double getDiskReadSpeed() {
        return diskReadSpeed;
    }

    /**
     * 设置磁盘读取速度
     *
     * @param diskReadSpeed 磁盘读取速度
     */
    public void setDiskReadSpeed(double diskReadSpeed) {
        this.diskReadSpeed = diskReadSpeed;
    }

    /**
     * 获取磁盘写入速度
     *
     * @return 磁盘写入速度
     */
    public double getDiskWriteSpeed() {
        return diskWriteSpeed;
    }

    /**
     * 设置磁盘写入速度
     *
     * @param diskWriteSpeed 磁盘写入速度
     */
    public void setDiskWriteSpeed(double diskWriteSpeed) {
        this.diskWriteSpeed = diskWriteSpeed;
    }

    /**
     * 获取网络发送速度
     *
     * @return 网络发送速度
     */
    public double getNetworkSendSpeed() {
        return networkSendSpeed;
    }

    /**
     * 设置网络发送速度
     *
     * @param networkSendSpeed 网络发送速度
     */
    public void setNetworkSendSpeed(double networkSendSpeed) {
        this.networkSendSpeed = networkSendSpeed;
    }

    /**
     * 获取网络接收速度
     *
     * @return 网络接收速度
     */
    public double getNetworkReceiveSpeed() {
        return networkReceiveSpeed;
    }

    /**
     * 设置网络接收速度
     *
     * @param networkReceiveSpeed 网络接收速度
     */
    public void setNetworkReceiveSpeed(double networkReceiveSpeed) {
        this.networkReceiveSpeed = networkReceiveSpeed;
    }

    /**
     * 获取cpu使用率
     *
     * @return cpu使用率
     */
    public double getCpuUsage() {
        return cpuUsage;
    }

    /**
     * 设置cpu使用率
     *
     * @param cpuUsage cpu使用率
     */
    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    /**
     * 获取内存使用率
     *
     * @return 内存使用率
     */
    public double getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * 设置内存使用率
     *
     * @param memoryUsage 内存使用率
     */
    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

}
