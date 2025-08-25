package cn.oyzh.easyshell.ssh2.server;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ShellServerInfo {

    /**
     * 文件限制
     */
    private String ulimit;

    /**
     * 系统架构
     */
    private String arch;

    /**
     * 系统名称
     */
    private String uname;

    /**
     * 启动时间
     */
    private String uptime;

    /**
     * 本地化信息
     */
    private String locale;

    /**
     * 时区信息
     */
    private String timezone;

    /**
     * shell名称
     */
    private String shellName;

    public String getTimezone() {
        return timezone;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    private double totalMemory;

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String getUlimit() {
        return ulimit;
    }

    public void setUlimit(String ulimit) {
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

    public double getTotalMemory() {
        return totalMemory;
    }

    public String getTotalMemoryInfo() {
        return totalMemory + "MB";
    }

    public void setTotalMemory(double totalMemory) {
        this.totalMemory = totalMemory;
    }

    public String getShellName() {
        return shellName;
    }

    public void setShellName(String shellName) {
        this.shellName = shellName;
    }
}
