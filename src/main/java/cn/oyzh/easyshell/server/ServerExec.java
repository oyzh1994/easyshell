package cn.oyzh.easyshell.server;

import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
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
        ServerMonitor monitor = this.monitorSimple();
        String arch = this.arch();
        int ulimit = this.ulimit();
        String uname = this.uname();
        double totalMemory = this.totalMemory();
        monitor.setArch(arch);
        monitor.setUname(uname);
        monitor.setUlimit(ulimit);
        monitor.setTotalMemory(ulimit);
        monitor.setTotalMemory(totalMemory);
        return monitor;
    }

    public ServerMonitor monitorSimple() {
        ServerMonitor monitor = new ServerMonitor();
        DownLatch latch = new DownLatch(3);

        ThreadUtil.startVirtual(() -> {
            try {
                double[] disk = this.vmstat_d();
                monitor.setReadSpeed(disk[0]);
                monitor.setWriteSpeed(disk[1]);
            } finally {
                latch.countDown();
            }
        });

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

        latch.await();
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
            if (memoryUsage.contains("%")) {
                memoryUsage = memoryUsage.replace("%", "");
            }
            return Double.parseDouble(memoryUsage);
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

    public double[] iostat_d() {
        try {
            String iostat = this.client.exec("/usr/bin/iostat -dkx 1 2 | /usr/bin/awk 'NR>3 && $1!=\"loop*\"'");
            if (StringUtil.isNotBlank(iostat)) {
                double readSpeed = 0;
                double writeSpeed = 0;
                int lineCount = 0;
                for (String line : iostat.split("\n")) {
                    if (!line.contains(".")) {
                        continue;
                    }
                    lineCount++;
                    String[] cols = line.split("\\s+");
                    readSpeed += Double.parseDouble(cols[3]);
                    writeSpeed += Double.parseDouble(cols[4]);
                }
                return new double[]{readSpeed / lineCount, writeSpeed / lineCount};
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return new double[]{-1L, -1L};
    }

    public double[] vmstat_d() {
        try {
            String vmstat = this.client.exec("/usr/bin/vmstat 1 2 | /usr/bin/awk 'NR==3 {print \"R:\", $6*0.5\",\", \"W:\", $7*0.5}'");
            String[] cols = vmstat.split(",");
            double readSpeed = Double.parseDouble(cols[0].split(":")[1].trim());
            double writeSpeed = Double.parseDouble(cols[1].split(":")[1].trim());
            return new double[]{readSpeed, writeSpeed};
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return new double[]{-1L, -1L};
    }
}
