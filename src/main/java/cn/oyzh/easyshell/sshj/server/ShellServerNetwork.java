//package cn.oyzh.easyshell.sshj.server;
//
///**
// * @author oyzh
// * @since 2025-03-16
// */
//public class ShellServerNetwork {
//
//    private long lastUpdateTime;
//
//    private double lastSend = -1;
//
//    private double lastReceive = -1;
//
//    public double[] calcSpeed(double[] data) {
//        double send = data[0];
//        double receive = data[1];
//        if (this.lastSend == -1 || send == -1 || receive == -1) {
//            this.lastSend = send;
//            this.lastReceive = receive;
//            this.lastUpdateTime = System.currentTimeMillis();
//            return new double[]{-1, -1};
//        } else {
//            long now = System.currentTimeMillis();
//            double cost = (now - this.lastUpdateTime) / 1000d;
//            double sendSpeed = (send - this.lastSend) / 1024d / cost;
//            double receiveSpeed = (receive - this.lastReceive) / 1024d / cost;
//            this.lastSend = send;
//            this.lastReceive = receive;
//            this.lastUpdateTime = System.currentTimeMillis();
//            return new double[]{sendSpeed, receiveSpeed};
//        }
//    }
//
//}
