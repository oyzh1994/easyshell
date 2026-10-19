package cn.oyzh.easyshell.ssh2.docker;

/**
 * docker资源
 *
 * @author oyzh
 * @since 2025-03-13
 */
public class ShellDockerResource {

    /**
     * 内存
     */
    private long memory;

    /**
     * 内存交换区
     */
    private long memorySwap;

    /**
     * cpu份额
     */
    private long cpuShares;

    /**
     * cpus核心
     */
    private long nanoCpus;

    /**
     * cpu时间
     */
    private long cpuPeriod;

    /**
     * cpu配额
     */
    private long cpuQuota;

    /**
     * 获取内存
     *
     * @return 内存
     */
    public long getMemory() {
        return memory;
    }

    /**
     * 设置内存
     *
     * @param memory 内存
     */
    public void setMemory(long memory) {
        this.memory = memory;
    }

    /**
     * 获取内存交换区
     *
     * @return 内存交换区
     */
    public long getMemorySwap() {
        return memorySwap;
    }

    /**
     * 设置内存交换区
     *
     * @param memorySwap 内存交换区
     */
    public void setMemorySwap(long memorySwap) {
        this.memorySwap = memorySwap;
    }

    /**
     * 获取cpu份额
     *
     * @return cpu份额
     */
    public long getCpuShares() {
        return cpuShares;
    }

    /**
     * 设置cpu份额
     *
     * @param cpuShares cpu份额
     */
    public void setCpuShares(long cpuShares) {
        this.cpuShares = cpuShares;
    }

    /**
     * 获取cpus核心
     *
     * @return cpus核心
     */
    public long getNanoCpus() {
        return nanoCpus;
    }

    /**
     * 设置cpus核心
     *
     * @param nanoCpus cpus核心
     */
    public void setNanoCpus(long nanoCpus) {
        this.nanoCpus = nanoCpus;
    }

    /**
     * 获取cpu时间
     *
     * @return cpu时间
     */
    public long getCpuPeriod() {
        return cpuPeriod;
    }

    /**
     * 设置cpu时间
     *
     * @param cpuPeriod cpu时间
     */
    public void setCpuPeriod(long cpuPeriod) {
        this.cpuPeriod = cpuPeriod;
    }

    /**
     * 获取cpu配额
     *
     * @return cpu配额
     */
    public long getCpuQuota() {
        return cpuQuota;
    }

    /**
     * 设置cpu配额
     *
     * @param cpuQuota cpu配额
     */
    public void setCpuQuota(long cpuQuota) {
        this.cpuQuota = cpuQuota;
    }
}
