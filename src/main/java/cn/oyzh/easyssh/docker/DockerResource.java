package cn.oyzh.easyssh.docker;

/**
 * @author oyzh
 * @since 2025-03-13
 */
public class DockerResource {

    private long memory;

    private long memorySwap;

    private long cpuShares;

    private long nanoCpus;

    private long cpuPeriod;

    private long cpuQuota;

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public long getMemorySwap() {
        return memorySwap;
    }

    public void setMemorySwap(long memorySwap) {
        this.memorySwap = memorySwap;
    }

    public long getCpuShares() {
        return cpuShares;
    }

    public void setCpuShares(long cpuShares) {
        this.cpuShares = cpuShares;
    }

    public long getNanoCpus() {
        return nanoCpus;
    }

    public void setNanoCpus(long nanoCpus) {
        this.nanoCpus = nanoCpus;
    }

    public long getCpuPeriod() {
        return cpuPeriod;
    }

    public void setCpuPeriod(long cpuPeriod) {
        this.cpuPeriod = cpuPeriod;
    }

    public long getCpuQuota() {
        return cpuQuota;
    }

    public void setCpuQuota(long cpuQuota) {
        this.cpuQuota = cpuQuota;
    }
}
