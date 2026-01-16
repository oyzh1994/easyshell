package cn.oyzh.easyshell.ssh2.process;

import cn.oyzh.common.object.ObjectCopier;

/**
 * 进程信息
 *
 * @author oyzh
 * @since 25/03/29
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

    public double getRss() {
        return rss;
    }

    public void setRss(double rss) {
        this.rss = rss;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getMemUsage() {
        return memUsage;
    }

    public void setMemUsage(double memUsage) {
        this.memUsage = memUsage;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getTime() {
        return time;
    }

    public String getTimeData() {
        if (this.time == null) {
            return "-";
        }
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getNetworkSend() {
        return networkSend;
    }

    public String getNetworkSendData() {
        if (this.networkSend == -1) {
            return "-";
        }
        return ShellProcessParser.formatSpeed(this.networkSend, 2);
    }

    public void setNetworkSend(double networkSend) {
        this.networkSend = networkSend;
    }

    public double getNetworkRecv() {
        return networkRecv;
    }

    public String getNetworkRecvData() {
        if (this.networkRecv == -1) {
            return "-";
        }
        return ShellProcessParser.formatSpeed(this.networkRecv, 2);
    }

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
