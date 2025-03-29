package cn.oyzh.easyshell.process;

import cn.oyzh.common.object.ObjectCopier;

/**
 * 进程信息
 *
 * @author oyzh
 * @since 25/03/29
 */
public class ProcessInfo implements ObjectCopier<ProcessInfo> {

    private String user;

    private int pid;

    private String stat;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String start;

    private String time;

    private double cpuUsage;

    private double memUsage;

    private String command;

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

    @Override
    public void copy(ProcessInfo t1) {
        this.time = t1.time;
        this.stat = t1.stat;
        this.start = t1.start;
        this.command = t1.command;
        this.memUsage = t1.memUsage;
        this.cpuUsage = t1.cpuUsage;
    }
}
