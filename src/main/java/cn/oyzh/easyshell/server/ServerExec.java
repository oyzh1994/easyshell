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

    public ServerInfo info() {
        ServerInfo info = new ServerInfo();
        long totalMemory = this.totalMemory();
        info.setTotalMemory(totalMemory);
        return info;
    }

    public ServerMonitor monitor() {
        ServerMonitor monitor = new ServerMonitor();
        double cpuUsage = this.cpuUsage();
        double memoryUsage = this.memoryUsage();
        double totalMemory = this.totalMemory();
        monitor.setCpuUsage(cpuUsage);
        monitor.setMemoryUsage(memoryUsage);
        monitor.setTotalMemory(totalMemory);
        return monitor;
    }

    public double cpuUsage() {
        try {
            String cpuUsage = this.client.exec("/bin/ps -aux | /usr/bin/awk '{sum+=$3} END {print sum}'");
            return Double.parseDouble(cpuUsage);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return -1;
    }

    public double memoryUsage() {
        try {
            String memoryUsage = this.client.exec("/usr/bin/free | /usr/bin/awk '/^Mem:/ {printf \"%.2f%\\n\", $3/$2 * 100.0}'");
            return Double.parseDouble(memoryUsage);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return -1;
    }

//    public double[] memoryUsage() {
//        String memoryUsage = this.client.exec("/usr/bin/free -m | /usr/bin/awk 'NR==2{printf \"Total Memory: %d, Memory Usage: %.2f%\\n\", $2, ($3/$2)*100}'");
//        String[] arr = memoryUsage.split(",");
//        double[] result = new double[arr.length];
//        result[0] = Double.parseDouble(arr[0].split(":")[1].trim());
//        result[1] = Double.parseDouble(arr[1].split(":")[1].trim());
//        return result;
//    }

    public long totalMemory() {
        try {
            String totalMemory = this.client.exec("/bin/cat /proc/meminfo | /usr/bin/awk '/MemTotal/ {print int($2/1024)}'");
            if (totalMemory.startsWith("\"")) {
                totalMemory = totalMemory.substring(1);
            }
            if (totalMemory.endsWith("\n")) {
                totalMemory = totalMemory.replace("\n", "");
            }
            return Long.parseLong(totalMemory);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return -1;
    }
}
