package cn.oyzh.easyshell.ssh2.process;

import cn.oyzh.common.object.ObjectCopier;

/**
 * 进程信息
 *
 * @author oyzh
 * @since 2025-03-29
 */
public class ShellProcessInfo implements ObjectCopier<ShellProcessInfo> {

    /**
     * 用户
     */
    private String user;

    /**
     * 进程id
     */
    private int pid;

    /**
     * 状态
     */
    private String stat;

    /**
     * 开始时间
     */
    private String start;

    /**
     * cpu总使用时间
     */
    private String time;

    /**
     * cpu使用率
     */
    private double cpuUsage;

    /**
     * 内存使用率
     */
    private double memUsage;

    /**
     * 启动命令
     */
    private String command;

    /**
     * rss
     */
    private double rss;

    /**
     * 网络接收
     */
    private double networkSend = -1;

    /**
     * 网络发送
     */
    private double networkRecv = -1;

    /**
     * 获取rss
     *
     * @return rss
     */
    public double getRss() {
        return rss;
    }

    /**
     * 设置rss
     *
     * @param rss rss
     */
    public void setRss(double rss) {
        this.rss = rss;
    }

    /**
     * 获取用户
     *
     * @return 用户
     */
    public String getUser() {
        return user;
    }

    /**
     * 设置用户
     *
     * @param user 用户
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * 获取状态
     *
     * @return 状态
     */
    public String getStat() {
        return stat;
    }

    /**
     * 设置状态
     *
     * @param stat 状态
     */
    public void setStat(String stat) {
        this.stat = stat;
    }

    /**
     * 获取启动命令
     *
     * @return 启动命令
     */
    public String getCommand() {
        return command;
    }

    /**
     * 设置启动命令
     *
     * @param command 启动命令
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * 获取进程id
     *
     * @return 进程id
     */
    public int getPid() {
        return pid;
    }

    /**
     * 设置进程id
     *
     * @param pid 进程id
     */
    public void setPid(int pid) {
        this.pid = pid;
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
    public double getMemUsage() {
        return memUsage;
    }

    /**
     * 设置内存使用率
     *
     * @param memUsage 内存使用率
     */
    public void setMemUsage(double memUsage) {
        this.memUsage = memUsage;
    }

    /**
     * 获取开始时间
     *
     * @return 开始时间
     */
    public String getStart() {
        return start;
    }

    /**
     * 设置开始时间
     *
     * @param start 开始时间
     */
    public void setStart(String start) {
        this.start = start;
    }

    /**
     * 获取cpu总使用时间
     *
     * @return cpu总使用时间
     */
    public String getTime() {
        return time;
    }

    /**
     * 获取cpu总使用时间，空值返回-
     *
     * @return cpu总使用时间
     */
    public String getTimeData() {
        if (this.time == null) {
            return "-";
        }
        return time;
    }

    /**
     * 设置cpu总使用时间
     *
     * @param time cpu总使用时间
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * 获取网络发送
     *
     * @return 网络发送
     */
    public double getNetworkSend() {
        return networkSend;
    }

    /**
     * 获取网络发送，空值返回-
     *
     * @return 网络发送
     */
    public String getNetworkSendData() {
        if (this.networkSend == -1) {
            return "-";
        }
        return ShellProcessParser.formatSpeed(this.networkSend, 2);
    }

    /**
     * 设置网络发送
     *
     * @param networkSend 网络发送
     */
    public void setNetworkSend(double networkSend) {
        this.networkSend = networkSend;
    }

    /**
     * 获取网络接收
     *
     * @return 网络接收
     */
    public double getNetworkRecv() {
        return networkRecv;
    }

    /**
     * 获取网络接收，空值返回-
     *
     * @return 网络接收
     */
    public String getNetworkRecvData() {
        if (this.networkRecv == -1) {
            return "-";
        }
        return ShellProcessParser.formatSpeed(this.networkRecv, 2);
    }

    /**
     * 设置网络接收
     *
     * @param networkRecv 网络接收
     */
    public void setNetworkRecv(double networkRecv) {
        this.networkRecv = networkRecv;
    }

    @Override
    public void copy(ShellProcessInfo t1) {
        this.rss = t1.rss;
        this.time = t1.time;
        this.user = t1.user;
        this.stat = t1.stat;
        this.start = t1.start;
        this.command = t1.command;
        this.memUsage = t1.memUsage;
        this.cpuUsage = t1.cpuUsage;
        this.networkSend = t1.networkSend;
        this.networkRecv = t1.networkRecv;
    }
}
