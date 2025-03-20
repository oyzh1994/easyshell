package cn.oyzh.easyshell.server;

import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.shell.ShellClient;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ServerExec {

    private final ShellClient client;

    public ServerExec(ShellClient client) {
        this.client = client;
    }

    /**
     * 服务器磁盘对象
     */
    private final ServerDisk disk = new ServerDisk();

    /**
     * 服务器网络对象
     */
    private final ServerNetwork network = new ServerNetwork();

    public ServerMonitor monitor() {
        ServerMonitor monitor = this.monitorSimple();
        String arch = this.arch();
        int ulimit = this.ulimit();
        String uname = this.uname();
        double totalMemory = this.totalMemory();
        String uptime = this.uptime();
        monitor.setArch(arch);
        monitor.setUname(uname);
        monitor.setUlimit(ulimit);
        monitor.setUptime(uptime);
        monitor.setTotalMemory(totalMemory);
        return monitor;
    }

    public ServerMonitor monitorSimple() {
        ServerMonitor monitor = new ServerMonitor();
        DownLatch latch = new DownLatch(4);

        ThreadUtil.startVirtual(() -> {
            try {
                double cpuUsage = this.cpuUsage();
                monitor.setCpuUsage(cpuUsage);
            } finally {
                latch.countDown();
            }
        });

        ThreadUtil.startVirtual(() -> {
            try {
                double memoryUsage = this.memoryUsage();
                monitor.setMemoryUsage(memoryUsage);
            } finally {
                latch.countDown();
            }
        });

        ThreadUtil.startVirtual(() -> {
            try {
                double[] data = this.disk();
                double[] speed = this.disk.calcSpeed(data);
                monitor.setDiskReadSpeed(speed[0]);
                monitor.setDiskWriteSpeed(speed[1]);
            } finally {
                latch.countDown();
            }
        });

        ThreadUtil.startVirtual(() -> {
            try {
                double[] data = this.network();
                double[] speed = this.network.calcSpeed(data);
                monitor.setNetworkSendSpeed(speed[0]);
                monitor.setNetworkReceiveSpeed(speed[1]);
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        return monitor;
    }

    public double cpuUsage() {
        try {
            String cpuUsage = this.client.exec("/usr/bin/top -bn1 | /usr/bin/grep \"Cpu(s)\" | /usr/bin/awk '{print $2 + $4}'");
//            String cpuUsage = this.client.exec("/bin/ps -aux | /usr/bin/awk '{sum+=$3} END {print sum}'");
            return Double.parseDouble(cpuUsage);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return -1;
    }

    public double memoryUsage() {
        try {
            String output = this.client.exec("/usr/bin/free | /usr/bin/awk '/^Mem:/ {printf \"%.2f%\\n\", $3/$2 * 100.0}'");
            if (StringUtil.isBlank(output)) {
                return -1;
            }
            if (output.contains("%")) {
                output = output.replace("%", "");
            }
            return Double.parseDouble(output);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return -1;
    }

    public int ulimit() {
        try {
            String ulimit = this.client.exec("ulimit -n");
            if (ulimit.endsWith("\n")) {
                ulimit = ulimit.replace("\n", "");
            }
            return Integer.parseInt(ulimit);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return -1;
    }

    public String uname() {
        try {
            return this.client.exec("/bin/uname -rs");
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return "N/A";
    }

    public String arch() {
        try {
            return this.client.exec("/bin/uname -m");
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return "N/A";
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

//    public double[] iostat_d() {
//        try {
//            String iostat = this.client.exec("/usr/bin/iostat -dkx 1 2 | /usr/bin/awk 'NR>3 && $1!=\"loop*\"'");
//            if (StringUtil.isNotBlank(iostat)) {
//                double readSpeed = 0;
//                double writeSpeed = 0;
//                int lineCount = 0;
//                for (String line : iostat.split("\n")) {
//                    if (!line.contains(".")) {
//                        continue;
//                    }
//                    lineCount++;
//                    String[] cols = line.split("\\s+");
//                    readSpeed += Double.parseDouble(cols[3]);
//                    writeSpeed += Double.parseDouble(cols[4]);
//                }
//                return new double[]{readSpeed / lineCount, writeSpeed / lineCount};
//            }
//        } catch (Exception ee) {
//            ee.printStackTrace();
//        }
//        return new double[]{-1L, -1L};
//    }
//
//    public double[] vmstat_d() {
//        try {
//            String vmstat = this.client.exec("/usr/bin/vmstat 1 2 | /usr/bin/awk 'NR==3 {print \"R:\", $6*0.5\",\", \"W:\", $7*0.5}'");
//            String[] cols = vmstat.split(",");
//            double readSpeed = Double.parseDouble(cols[0].split(":")[1].trim());
//            double writeSpeed = Double.parseDouble(cols[1].split(":")[1].trim());
//            return new double[]{readSpeed, writeSpeed};
//        } catch (Exception ee) {
//            ee.printStackTrace();
//        }
//        return new double[]{-1L, -1L};
//    }

    public double[] disk() {
        try {
            String output = this.client.exec("/bin/cat /proc/diskstats");
            if (StringUtil.isBlank(output)) {
                return new double[]{-1L, -1L};
            }
            String[] lines = output.split("\n");
            double read = 0;
            double write = 0;
            List<String> handleIds = new ArrayList<>();
            for (String line : lines) {
                String[] cols = line.trim().split("\\s+");
                String mainId = cols[0];
                if (handleIds.contains(mainId)) {
                    continue;
                }
                String readTotal = cols[5];
                String writeTotal = cols[9];
                read += Double.parseDouble(readTotal);
                write += Double.parseDouble(writeTotal);
                handleIds.add(mainId);
            }
            return new double[]{read, write};
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return new double[]{-1L, -1L};
    }

    public double[] network() {
        try {
            String output = this.client.exec("/bin/cat /proc/net/dev | /bin/grep -vE 'lo|^[ ]*$' | /usr/bin/awk -F: '{print $2 \" \" $10}' | /usr/bin/awk '{print $1 \" \" $2}'\n");
            if (StringUtil.isBlank(output)) {
                return new double[]{-1L, -1L};
            }
            String[] lines = output.split("\n");
            double send = 0;
            double receive = 0;
            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }
                String[] cols = line.split("\\s+");
                receive += Double.parseDouble(cols[0]);
                send += Double.parseDouble(cols[1]);
            }
            return new double[]{send, receive};
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return new double[]{-1L, -1L};
    }

    public String uptime() {
        try {
            String output = this.client.exec("/usr/bin/uptime");
            if (StringUtil.isNotBlank(output)) {
                output = output.substring(output.indexOf("up"));
                output = output.substring(0, output.indexOf(","));
                return output.trim();
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return "N/A";
    }
}
