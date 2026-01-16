package cn.oyzh.easyshell.ssh2.server;

/**
 * @author oyzh
 * @since 2025-03-16
 */
public class ShellServerNetwork {

    /**
     * 最后更新时间
     */
    private long lastUpdateTime;

    /**
     * 最后发送值
     */
    private double lastSend = -1;

    /**
     * 最后接收值
     */
    private double lastReceive = -1;

    public double[] calcSpeed(double[] data) {
        double send = data[0];
        double receive = data[1];
        if (this.lastSend == -1 || send == -1 || receive == -1 || send < this.lastSend || receive < this.lastReceive) {
            this.lastSend = send;
            this.lastReceive = receive;
            this.lastUpdateTime = System.currentTimeMillis();
            return new double[]{-1, -1};
        } else {
            long now = System.currentTimeMillis();
            double cost = (now - this.lastUpdateTime) / 1000d;
            double sendSpeed = (send - this.lastSend) / 1024d / cost;
            double receiveSpeed = (receive - this.lastReceive) / 1024d / cost;
            this.lastSend = send;
            this.lastReceive = receive;
            this.lastUpdateTime = System.currentTimeMillis();
            return new double[]{sendSpeed, receiveSpeed};
        }
    }

}
