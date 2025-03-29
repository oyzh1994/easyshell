package cn.oyzh.easyshell.server;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.shell.ShellClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ServerExec implements AutoCloseable {

    /**
     * shell客户端
     */
    private ShellClient client;

    /**
     * 服务器磁盘对象
     */
    private ServerDisk disk;

    /**
     * 服务器网络对象
     */
    private ServerNetwork network;

    public ServerExec(ShellClient client) {
        this.client = client;
        this.disk = new ServerDisk();
        this.network = new ServerNetwork();
    }

    public ServerMonitor monitor() {
        ServerMonitor monitor = this.monitorSimple();
        String arch = this.arch();
        String uname = this.uname();
        String ulimit = this.ulimit();
        String uptime = this.uptime();
        double totalMemory = this.totalMemory();
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
                if (this.client.isUnix()) {
                    monitor.setDiskReadSpeed(data[0]);
                    monitor.setDiskWriteSpeed(data[1]);
                } else {
                    double[] speed = this.disk.calcSpeed(data);
                    monitor.setDiskReadSpeed(speed[0]);
                    monitor.setDiskWriteSpeed(speed[1]);
                }
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
            if (this.client.isMacos()) {
                String cpuUsage = this.client.exec("top -l 1 -s 0 | sed -n '4p'");
                cpuUsage = cpuUsage.substring(cpuUsage.lastIndexOf(",") + 1, cpuUsage.lastIndexOf("%")).trim();
                return 100 - Double.parseDouble(cpuUsage);
            }
            if (this.client.isUnix()) {
                String cpuUsage = this.client.exec("vmstat 1 2 | tail -1 | awk '{print 100 - $15}'");
                return Double.parseDouble(cpuUsage);
            }
            String cpuUsage = this.client.exec("top -bn1 | grep \"Cpu(s)\" | awk '{print $2 + $4}'");
            if (StringUtil.isBlank(cpuUsage)) {
                cpuUsage = this.client.exec("ps -aux | awk '{sum+=$3} END {print sum}'");
            }
            return Double.parseDouble(cpuUsage);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return -1;
    }

    public double memoryUsage() {
        try {
            if (this.client.isMacos()) {
                String output = this.client.exec("vm_stat | awk '/Pages active/ {active = $3} /Pages inactive/ {inactive = $3} /Pages speculative/ {speculative = $3} /Pages wired down/ {wired = $3} END {total = active + inactive + speculative + wired; used = active + wired; printf \"%.2f%%\\n\", (used / total) * 100}'");
                if (StringUtil.isBlank(output)) {
                    return -1;
                }
                if (output.contains("%")) {
                    output = output.replace("%", "");
                }
                return Double.parseDouble(output);
            }
            if (this.client.isUnix()) {
//                String output = this.client.exec("top -b -n 1 | awk '/Mem:/ {printf \"%.2f%%\\n\", ($3 / $2) * 100}'");
//                if (StringUtil.isBlank(output)) {
//                    return -1;
//                }
//                if (output.contains("%")) {
//                    output = output.replace("%", "");
//                }
//                return Double.parseDouble(output);
                String output = this.client.exec("sysctl -n hw.physmem vm.stats.vm.v_free_count");
                if (StringUtil.isBlank(output)) {
                    return -1;
                }
                String[] array = output.lines().toArray(String[]::new);
                Double used = Double.parseDouble(array[1]) * 4096;
                Double total = Double.parseDouble(array[0]);
                return (1 - used / total) * 100;
            }
            String output = this.client.exec("free | awk '/^Mem:/ {printf \"%.2f%%\\n\", $3/$2 * 100.0}'");
            // TODO: 针对deepin可能失效问题
            if (StringUtil.isBlank(output)) {
                output = this.client.exec("free");
                if (StringUtil.isNotBlank(output)) {
                    String[] lines = output.lines().toArray(String[]::new);
                    if (lines.length > 2) {
                        String[] cols = lines[1].split("\\s+");
                        double used = Double.parseDouble(cols[2]);
                        double total = Double.parseDouble(cols[1]);
                        return (used / total) * 100;
                    }
                }
            }
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

    public String ulimit() {
        try {
            if (!this.client.isWindows()) {
                String ulimit = this.client.exec("ulimit -n");
                if (ulimit.endsWith("\n")) {
                    ulimit = ulimit.replace("\n", "");
                }
                return ulimit;
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return "N/A";
    }

    public String uname() {
        try {
            if (this.client.isWindows()) {
                String output = this.client.exec("hostname");
                return output == null ? "N/A" : output.trim();
            }
            return this.client.exec("uname -rs");
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return "N/A";
    }

    public String arch() {
        try {
            if (this.client.isWindows()) {
                String output = this.client.exec("wmic os get osarchitecture");
                String arch = ArrayUtil.indexOf(output.split("\n"), 1);
                return arch == null ? "N/A" : arch.trim();
            }
            return this.client.exec("uname -m");
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return "N/A";
    }

    public long totalMemory() {
        try {
            if (this.client.isMacos()) {
                String totalMemory = this.client.exec("sysctl -n hw.memsize");
                return Long.parseLong(totalMemory) / 1024 / 1024;
            }
            if (this.client.isWindows()) {
                String totalMemory = this.client.exec("wmic memorychip get capacity");
                totalMemory = ArrayUtil.indexOf(totalMemory.split("\n"), 1);
                if (totalMemory == null) {
                    return -1;
                }
                return Long.parseLong(totalMemory.trim()) / 1024 / 1024;
            }
            if (this.client.isUnix()) {
                String totalMemory = this.client.exec("sysctl hw.physmem");
                totalMemory = totalMemory.split(":")[1].trim();
                return Long.parseLong(totalMemory) / 1024 / 1024;
            }
            String totalMemory = this.client.exec("cat /proc/meminfo | awk '/MemTotal/ {print int($2/1024)}'");
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

    public double[] disk() {
        try {
            if (this.client.isMacos()) {
                String output = this.client.exec("top -l 1 -s 0 | grep -E '^Disks'");
                if (StringUtil.isBlank(output)) {
                    return new double[]{-1L, -1L};
                }
                String r = output.substring(output.indexOf(":") + 1, output.indexOf("/")).trim();
                String w = output.substring(output.indexOf(",") + 1, output.lastIndexOf("/")).trim();
                double read = Double.parseDouble(r);
                double write = Double.parseDouble(w);
                return new double[]{read, write};
            }
//            if (this.client.isUnix()) {
//                String output = this.client.exec("iostat -d -x 1 1");
//                if (StringUtil.isBlank(output)) {
//                    return new double[]{-1L, -1L};
//                }
//                String[] lines = output.split("\n");
//                double read = 0;
//                double write = 0;
//                for (int i = 2; i < lines.length; i++) {
//                    String line = lines[i];
//                    String[] cols = line.trim().split("\\s+");
//                    String readTotal = cols[3];
//                    String writeTotal = cols[4];
//                    read += Double.parseDouble(readTotal);
//                    write += Double.parseDouble(writeTotal);
//                }
//                return new double[]{read * 1024, write * 1024};
//            }
            if (this.client.isUnix()) {
                String output = this.client.exec("timeout 2.5s gstat");
                if (StringUtil.isBlank(output)) {
                    return new double[]{-1L, -1L};
                }
                String[] lines = output.split("\n");
                double read = 0;
                double write = 0;
                for (int i = 2; i < lines.length; i++) {
                    String line = lines[i];
                    String[] cols = line.trim().split("\\s+");
                    String readTotal = cols[4];
                    String writeTotal = cols[7];
                    read += Double.parseDouble(readTotal);
                    write += Double.parseDouble(writeTotal);
                }
                return new double[]{read, write};
            }
            String output = this.client.exec("cat /proc/diskstats");
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
            if (this.client.isMacos()) {
                String output = this.client.exec("top -l 1 -s 0 | grep -E '^Networks'");
                if (StringUtil.isBlank(output)) {
                    return new double[]{-1L, -1L};
                }
                int fIndex = output.indexOf(":");
                String in = output.substring(output.indexOf(":", fIndex + 1) + 1, output.indexOf("/")).trim();
                String out = output.substring(output.indexOf(",") + 1, output.lastIndexOf("/")).trim();
                double send = Double.parseDouble(out);
                double receive = Double.parseDouble(in);
                return new double[]{send, receive};
            }
            if (this.client.isUnix()) {
                String output = this.client.exec("netstat -i");
                if (StringUtil.isBlank(output)) {
                    return new double[]{-1L, -1L};
                }
                String[] lines = output.split("\n");
                double send = 0;
                double receive = 0;
                for (int i = 1; i < lines.length; i++) {
                    String line = lines[i];
                    String[] cols = line.split("\\s+");
                    receive += Double.parseDouble(cols[4]);
                    send += Double.parseDouble(cols[7]);
                }
                return new double[]{send, receive};
            }
            String output = this.client.exec("cat /proc/net/dev | grep -vE 'lo|^[ ]*$' | awk -F: '{print $2 \" \" $10}' | awk '{print $1 \" \" $2}'\n");
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
            if (this.client.isWindows()) {
                String output = this.client.exec("wmic path Win32_OperatingSystem get LastBootUpTime");
                if (StringUtil.isNotBlank(output)) {
                    output = ArrayUtil.indexOf(output.split("\n"), 1);
                    if (StringUtil.isNotBlank(output)) {
                        Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(output.trim());
                        return "up at " + DateHelper.formatTimeSimple(date);
                    }
                }
            } else {
                String output = this.client.exec("uptime");
                if (StringUtil.isNotBlank(output)) {
                    output = output.substring(output.indexOf("up"));
                    output = output.substring(0, output.indexOf(","));
                    return output.trim();
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return "N/A";
    }

    @Override
    public void close() throws Exception {
        this.disk = null;
        this.client = null;
        this.network = null;
    }
}
