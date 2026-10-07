package cn.oyzh.easyshell.ssh2.process;


/**
 * 进程属性
 *
 * @author oyzh
 * @since 2025-03-30
 */
public class ShellProcessAttr {

    /**
     * pid
     */
    private String pid;

    /**
     * 状态
     */
    private String stat;

    /**
     * 用户名
     */
    private String user;

    /**
     * 获取pid
     *
     * @return pid
     */
    public String getPid() {
        return pid;
    }

    /**
     * 设置pid
     *
     * @param pid pid
     */
    public void setPid(String pid) {
        this.pid = pid;
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
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUser() {
        return user;
    }

    /**
     * 设置用户名
     *
     * @param user 用户名
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * 上一次更新时间
     */
    private long lastTime;

    /**
     * 上一次cpu使用率
     */
    private double lastCpuUsage;

    /**
     * 计算cpu使用率
     *
     * @param cpuUsage 当前cpu使用率
     * @return 结果
     */
    public double calcCpuUsage(double cpuUsage) {
        if (lastTime == 0) {
            this.lastTime = System.currentTimeMillis();
            this.lastCpuUsage = cpuUsage;
            return 0;
        }
        long now = System.currentTimeMillis();
        double cpuUsagePercent = (this.lastCpuUsage - cpuUsage) / (this.lastTime - now) * 1000;
        this.lastCpuUsage = cpuUsage;
        this.lastTime = now;
        return Math.abs(cpuUsagePercent);
    }
}
