package cn.oyzh.easyshell.server;

import cn.oyzh.easyshell.shell.ShellClient;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ServerExec {

    private final ShellClient client;

    public ServerExec(ShellClient client) {
        this.client = client;
    }

    public ServerMonitor monitor() {
        ServerMonitor monitor = new ServerMonitor();
        double cpuUsage = this.cpuUsage();
        double[] memoryUsage = this.memoryUsage();
        monitor.setCpuUsage(cpuUsage);
        monitor.setTotalMemory(memoryUsage[0]);
        monitor.setMemoryUsage(memoryUsage[1]);
        return monitor;
    }

    public double cpuUsage() {
        String cpuUsage = this.client.exec("ps -aux | awk '{sum+=$3} END {print sum}'");
        return Double.parseDouble(cpuUsage);
    }

    public double[] memoryUsage() {
        String memoryUsage = this.client.exec("free -m | awk 'NR==2{printf \"Total Memory: %d, Memory Usage: %.2f%\\n\", $2, ($3/$2)*100}'");
        String[] arr = memoryUsage.split(",");
        double[] result = new double[arr.length];
        result[0] = Double.parseDouble(arr[0].split(":")[1].trim());
        result[1] = Double.parseDouble(arr[1].split(":")[1].trim());
        return result;
    }
}
