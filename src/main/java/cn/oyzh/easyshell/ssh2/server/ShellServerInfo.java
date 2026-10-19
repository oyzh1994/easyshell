package cn.oyzh.easyshell.ssh2.server;

/**
 * 服务器信息
 *
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

    /**
     * 获取时区信息
     *
     * @return 时区信息
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * 获取本地化信息
     *
     * @return 本地化信息
     */
    public String getLocale() {
        return locale;
    }

    /**
     * 设置本地化信息
     *
     * @param locale 本地化信息
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * 设置时区信息
     *
     * @param timezone 时区信息
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * 总内存
     */
    private double totalMemory;

    /**
     * 获取启动时间
     *
     * @return 启动时间
     */
    public String getUptime() {
        return uptime;
    }

    /**
     * 设置启动时间
     *
     * @param uptime 启动时间
     */
    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    /**
     * 获取文件限制
     *
     * @return 文件限制
     */
    public String getUlimit() {
        return ulimit;
    }

    /**
     * 设置文件限制
     *
     * @param ulimit 文件限制
     */
    public void setUlimit(String ulimit) {
        this.ulimit = ulimit;
    }

    /**
     * 获取系统架构
     *
     * @return 系统架构
     */
    public String getArch() {
        return arch;
    }

    /**
     * 设置系统架构
     *
     * @param arch 系统架构
     */
    public void setArch(String arch) {
        this.arch = arch;
    }

    /**
     * 获取系统名称
     *
     * @return 系统名称
     */
    public String getUname() {
        return uname;
    }

    /**
     * 设置系统名称
     *
     * @param uname 系统名称
     */
    public void setUname(String uname) {
        this.uname = uname;
    }

    /**
     * 获取总内存
     *
     * @return 总内存
     */
    public double getTotalMemory() {
        return totalMemory;
    }

    /**
     * 获取总内存信息
     *
     * @return 总内存信息
     */
    public String getTotalMemoryInfo() {
        return totalMemory + "MB";
    }

    /**
     * 设置总内存
     *
     * @param totalMemory 总内存
     */
    public void setTotalMemory(double totalMemory) {
        this.totalMemory = totalMemory;
    }

    /**
     * 获取shell名称
     *
     * @return shell名称
     */
    public String getShellName() {
        return shellName;
    }

    /**
     * 设置shell名称
     *
     * @param shellName shell名称
     */
    public void setShellName(String shellName) {
        this.shellName = shellName;
    }
}
