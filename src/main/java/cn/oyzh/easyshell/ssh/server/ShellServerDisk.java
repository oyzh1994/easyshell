package cn.oyzh.easyshell.ssh.server;

/**
 * @author oyzh
 * @since 2025-03-16
 */
public class ShellServerDisk {

    private long lastUpdateTime;

    private double lastRead = -1;

    private double lastWrite = -1;

    public double[] calcSpeed(double[] data) {
        double read = data[0];
        double write = data[1];
        if (this.lastRead == -1 || read == -1 || write == -1) {
            this.lastRead = read;
            this.lastWrite = write;
            this.lastUpdateTime = System.currentTimeMillis();
            return new double[]{-1, -1};
        } else {
            long now = System.currentTimeMillis();
            double cost = (now - this.lastUpdateTime) / 1000d;
            double readSpeed = (read - this.lastRead) * 512 / 1024 / 1024d / cost;
            double writeSpeed = (write - this.lastWrite) * 512 / 1024 / 1024d / cost;
            this.lastRead = read;
            this.lastWrite = write;
            this.lastUpdateTime = now;
            return new double[]{readSpeed, writeSpeed};
        }
    }
}
