package cn.oyzh.easyshell.ssh2.server;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.util.ShellUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ShellServerExec implements AutoCloseable {

    /**
     * shell客户端
     */
    private ShellSSHClient client;

    /**
     * 服务器磁盘对象
     */
    private ShellServerDisk disk;

    /**
     * 服务器网络对象
     */
    private ShellServerNetwork network;

    public ShellSSHClient getClient() {
        return client;
    }

    public ShellServerExec(ShellSSHClient client) {
        this.client = client;
        this.disk = new ShellServerDisk();
        this.network = new ShellServerNetwork();
    }

    /**
     * 获取服务信息
     *
     * @return 服务信息
     */
    public ShellServerInfo info() {
        ShellServerInfo info = new ShellServerInfo();
        String arch = this.arch();
        String uname = this.uname();
        String ulimit = this.ulimit();
        String uptime = this.uptime();
        String locale = this.locale();
        String timezone = this.timezone();
        double totalMemory = this.totalMemory();
        String shellName = this.client.getShellName();
        info.setArch(arch);
        info.setUname(uname);
        info.setUlimit(ulimit);
        info.setUptime(uptime);
        info.setLocale(locale);
        info.setTimezone(timezone);
        info.setShellName(shellName);
        info.setTotalMemory(totalMemory);
        return info;
    }

//    public ShellServerMonitor monitor() {
//        ShellServerMonitor monitor = this.monitorSimple();
//        String arch = this.arch();
//        String uname = this.uname();
//        String ulimit = this.ulimit();
//        String uptime = this.uptime();
//        double totalMemory = this.totalMemory();
//        monitor.setArch(arch);
//        monitor.setUname(uname);
//        monitor.setUlimit(ulimit);
//        monitor.setUptime(uptime);
//        monitor.setTotalMemory(totalMemory);
//        return monitor;
//    }

    /**
     * 上一次缓存时间
     */
    private long lastMonitorTime;

    /**
     * 缓存记录
     */
    private ShellServerMonitor lastMonitor;

    /**
     * 获取服务监控信息
     *
     * @return 服务监控信息
     */
    public ShellServerMonitor monitor() {
        // 如果缓存的记录在指定时间内，则返回缓存记录
        if (this.lastMonitor != null && System.currentTimeMillis() - this.lastMonitorTime < 3000) {
            return this.lastMonitor;
        }
        ShellServerMonitor monitor = new ShellServerMonitor();
        DownLatch latch = DownLatch.of(4);

        ThreadUtil.start(() -> {
            try {
                double cpuUsage = this.cpuUsage();
                monitor.setCpuUsage(cpuUsage);
            } finally {
                latch.countDown();
            }
        });

        ThreadUtil.start(() -> {
            try {
                double memoryUsage = this.memoryUsage();
                monitor.setMemoryUsage(memoryUsage);
            } finally {
                latch.countDown();
            }
        });

        ThreadUtil.start(() -> {
            try {
                double[] data = this.disk();
                if (this.client.isUnix() || this.client.isWindows()) {
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

        ThreadUtil.start(() -> {
            try {
                double[] data = this.network();
                if (this.client.isWindows()) {
                    monitor.setNetworkSendSpeed(data[0]);
                    monitor.setNetworkReceiveSpeed(data[1]);
                } else {
                    double[] speed = this.network.calcSpeed(data);
                    monitor.setNetworkSendSpeed(speed[0]);
                    monitor.setNetworkReceiveSpeed(speed[1]);
                }
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        this.lastMonitor = monitor;
        this.lastMonitorTime = System.currentTimeMillis();
        return monitor;
    }

    /**
     * 获取cpu使用率
     *
     * @return cpu使用率
     */
    public double cpuUsage() {
        try {
            if (this.client.isMacos()) {
                String cpuUsage = this.client.exec("top -l 1 -s 0 | sed -n '4p'");
                cpuUsage = cpuUsage.substring(cpuUsage.lastIndexOf(",") + 1, cpuUsage.lastIndexOf("%")).trim();
                return 100 - Double.parseDouble(cpuUsage);
            }
            if (this.client.isWindows()) {
                String cpuUsage = this.client.exec("wmic cpu get loadpercentage", 1500);
                cpuUsage = ShellUtil.getWindowsCommandResult(cpuUsage);
                if (StringUtil.isBlank(cpuUsage)) {
                    return -1;
                }
                return Double.parseDouble(cpuUsage);
            }
            if (this.client.isUnix()) {
                String cpuUsage = this.client.exec("vmstat 1 2 | tail -1 | awk '{print $15\"=\"$19}'\n");
                String[] arr = cpuUsage.split("=");
                double usage;
                if (arr.length == 2) {
                    String val1 = arr[0];
                    String val2 = arr[1];
                    // 较新版本
                    if (RegexUtil.isNumber(val2) || RegexUtil.isDecimal(val2)) {
                        usage = 100 - Double.parseDouble(val2);
                    } else {// 早期版本
                        usage = 100 - Double.parseDouble(val1);
                    }
                } else {
                    usage = 100 - Double.parseDouble(arr[0]);
                }
                if (usage < 0 || usage > 100) {
                    return -1;
                }
                return usage;
            }
            String cpuUsage = this.client.exec("top -bn1 | grep \"Cpu(s)\" | awk '{print $2 + $4}'");
            if (StringUtil.isBlank(cpuUsage)) {
                cpuUsage = this.client.exec("ps -aux | awk '{sum+=$3} END {print sum}'");
            }
            return Double.parseDouble(cpuUsage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取内存使用率
     *
     * @return 内存使用率
     */
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
            if (this.client.isWindows()) {
                String output = this.client.exec("wmic OS get FreePhysicalMemory,TotalVisibleMemorySize /value", 500);
                if (StringUtil.isBlank(output)) {
                    return -1;
                }
                String[] arr = output.split("\n");
                long free = -1;
                long total = -1;
                for (String s : arr) {
                    if (StringUtil.startWithAnyIgnoreCase(s, "FreePhysicalMemory")) {
                        free = Long.parseLong(s.split("=")[1].trim());
                    } else if (StringUtil.startWithAnyIgnoreCase(s, "TotalVisibleMemorySize")) {
                        total = Long.parseLong(s.split("=")[1].trim());
                    }
                    if (free != -1 && total != -1) {
                        break;
                    }
                }
                return (total - free) * 100D / free;
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取文件限制
     *
     * @return 文件限制
     */
    public String ulimit() {
        try {
            if (!this.client.isWindows()) {
                String ulimit = this.client.exec("ulimit -n");
                if (ulimit.endsWith("\n")) {
                    ulimit = ulimit.replace("\n", "");
                }
                return ulimit;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "N/A";
    }

    /**
     * 获取系统名称
     *
     * @return 系统名称
     */
    public String uname() {
        try {
            if (this.client.isWindows()) {
                String output = this.client.exec("hostname");
                return output == null ? "N/A" : output.trim();
            }
            return this.client.exec("uname -rs");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "N/A";
    }

    /**
     * 获取系统架构
     *
     * @return 系统架构
     */
    public String arch() {
        try {
            if (this.client.isWindows()) {
                String output = this.client.exec("wmic os get osarchitecture", 500);
                if (StringUtil.isBlank(output)) {
                    return "N/A";
                }
                String arch = ArrayUtil.indexOf(output.split("\n"), 1);
                return arch == null ? "N/A" : arch.trim();
            }
            return this.client.exec("uname -m");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "N/A";
    }

    /**
     * 获取内存大小
     *
     * @return 内存大小
     */
    public long totalMemory() {
        try {
            if (this.client.isMacos()) {
                String totalMemory = this.client.exec("sysctl -n hw.memsize");
                return Long.parseLong(totalMemory) / 1024 / 1024;
            }
            if (this.client.isWindows()) {
                String totalMemory = this.client.exec("wmic memorychip get capacity", 500);
                if (StringUtil.isBlank(totalMemory)) {
                    return -1;
                }
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取磁盘读写信息
     *
     * @return 磁盘读写信息
     */
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
            if (this.client.isWindows()) {
                String output = this.client.exec("typeperf \"\\PhysicalDisk(*)\\Disk Read Bytes/sec\" \"\\PhysicalDisk(*)\\Disk Write Bytes/sec\" -sc 1");
                if (StringUtil.isBlank(output)) {
                    return new double[]{-1L, -1L};
                }
                String[] arr = output.split("\n");
                if (arr.length < 3) {
                    return new double[]{-1L, -1L};
                }
                output = arr[2].trim();
                arr = output.split(",");
                double read = 0;
                double write = 0;
                int mid = (arr.length - 1) / 2;
                for (int i = 1; i < arr.length; i++) {
                    String col = arr[i].trim().substring(1, arr[i].trim().length() - 1);
                    if (i <= mid) {
                        read += Double.parseDouble(col);
                    } else {
                        write += Double.parseDouble(col);
                    }
                }
                return new double[]{read / 1024 / 1024, write / 1024 / 1024};
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new double[]{-1L, -1L};
    }

    /**
     * 获取网络使用情况
     *
     * @return 网络使用情况
     */
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
            if (this.client.isWindows()) {
                String output = this.client.exec("typeperf \"\\Network Interface(*)\\Bytes Received/sec\" \"\\Network Interface(*)\\Bytes Sent/sec\" -sc 1");
                if (StringUtil.isBlank(output)) {
                    return new double[]{-1L, -1L};
                }
                String[] arr = output.split("\n");
                if (arr.length < 3) {
                    return new double[]{-1L, -1L};
                }
                output = arr[2].trim();
                arr = output.split(",");
                double send = 0;
                double receive = 0;
                int mid = (arr.length - 1) / 2;
                for (int i = 1; i < arr.length; i++) {
                    String col = arr[i].trim().substring(1, arr[i].trim().length() - 1);
                    if (i <= mid) {
                        receive += Double.parseDouble(col);
                    } else {
                        send += Double.parseDouble(col);
                    }
                }
                return new double[]{send / 1024, receive / 1024};
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new double[]{-1L, -1L};
    }

    /**
     * 获取主机启动时间
     *
     * @return 启动时间
     */
    public String uptime() {
        try {
            if (this.client.isWindows()) {
                String output = this.client.exec("wmic path Win32_OperatingSystem get LastBootUpTime", 500);
                if (StringUtil.isNotBlank(output)) {
                    output = ArrayUtil.indexOf(output.split("\n"), 1);
                    if (StringUtil.isNotBlank(output)) {
                        Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(output.trim());
                        return "up at " + DateHelper.formatDateTimeSimple(date);
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "N/A";
    }

    /**
     * 移动文件
     *
     * @param src 源文件
     * @param dst 目标文件
     * @return 结果
     */
    public String move(String src, String dst) {
        try {
            if (this.client.isLinux()
                    || this.client.isMacos()
                    || this.client.isFreeBSD()) {
                return this.client.exec("mv -f \"" + src + "\" \"" + dst + "\"");
            }
            if (this.client.isWindows()) {
                src = ShellFileUtil.fixWindowsFilePath(src);
                dst = ShellFileUtil.fixWindowsFilePath(dst);
                this.client.exec("move /Y \"" + src + "\" " + dst + "\"");
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 复制文件
     *
     * @param src 源文件
     * @param dst 目标文件
     * @return 结果
     */
    public String copy(String src, String dst) {
        try {
            if (this.client.isLinux()
                    || this.client.isMacos()
                    || this.client.isFreeBSD()) {
                return this.client.exec("cp -rf \"" + src + "\" \"" + dst + "\"");
            }
            if (this.client.isWindows()) {
                src = ShellFileUtil.fixWindowsFilePath(src);
                dst = ShellFileUtil.fixWindowsFilePath(dst);
                this.client.exec("copy /Y \"" + src + "\" \"" + dst + "\"");
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


    /**
     * 强制删除
     *
     * @param files 文件
     * @return 结果
     */
    public String forceDel(List<String> files) {
        if (this.client.isLinux()
                || this.client.isMacos()
                || this.client.isFreeBSD()) {
            StringBuilder builder = new StringBuilder();
            for (String file : files) {
                builder.append(" ").append("\"").append(file).append("\"");
            }
            return this.client.exec("rm -rf " + builder.substring(1));
        }
        return null;
    }

    /**
     * 强制删除
     *
     * @param file   文件
     * @param isFile 是否文件
     * @return 结果
     */
    public String forceDel(String file, boolean isFile) {
        try {
            if (this.client.isLinux()
                    || this.client.isMacos()
                    || this.client.isFreeBSD()) {
                if (isFile) {
                    return this.client.exec("rm -f \"" + file + "\"");
                }
                return this.client.exec("rm -rf \"" + file + "\"");
            }
            if (this.client.isWindows()) {
                file = ShellFileUtil.fixWindowsFilePath(file);
                if (isFile) {
                    return this.client.exec("del /f /q \"" + file + "\"");
                }
                return this.client.exec("rmdir /s /q \"" + file + "\"");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 压缩
     *
     * @param file 文件
     * @param type 类型
     * @return 结果
     */
    public String compress(String file, String type) {
        try {
            if (this.client.isLinux()) {
                String fName = ShellFileUtil.name(file);
                String pName = ShellFileUtil.parent(file);
                String cmd = "";
                if (StringUtil.equalsAnyIgnoreCase(type, "tgz", "tar.gz", "tar")) {
                    String compressName = fName + "." + type;
                    cmd = "cd " + pName + " && tar -zcvf " + compressName + " " + fName;
                } else if (StringUtil.equalsAnyIgnoreCase(type, "xz")) {
                    String compressName = fName + ".tar." + type;
                    cmd = "cd " + pName + " && tar -Jcvf " + compressName + " " + fName;
                } else if (StringUtil.equalsAnyIgnoreCase(type, "bz2")) {
                    String compressName = fName + ".tar." + type;
                    cmd = "cd " + pName + " && tar -jcvf " + compressName + " " + fName;
                } else if (StringUtil.equalsAnyIgnoreCase(type, "lz")) {
                    String compressName = fName + ".tar." + type;
                    cmd = "cd " + pName + " && tar --lzma " + compressName + " " + fName;
                } else if (StringUtil.equalsAnyIgnoreCase(type, "lzo")) {
                    String compressName = fName + ".tar." + type;
                    cmd = "cd " + pName + " && tar --lzop " + compressName + " " + fName;
                } else if (StringUtil.equalsAnyIgnoreCase(type, "zst")) {
                    String compressName = fName + ".tar." + type;
                    cmd = "cd " + pName + " && tar --zstd " + compressName + " " + fName;
                } else if (StringUtil.equalsAnyIgnoreCase(type, "rar")) {
                    String compressName = fName + "." + type;
                    cmd = "cd " + pName + " && rar a " + compressName + " " + fName;
                } else if (StringUtil.equalsAnyIgnoreCase(type, "7z", "zip")) {
                    String compressName = fName + "." + type;
                    cmd = "cd " + pName + " && 7z a " + compressName + " " + fName;
                }
                return this.client.exec(cmd);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 解压
     *
     * @param file 文件
     * @return 结果
     */
    public String uncompress(String file) {
        try {
            if (this.client.isLinux()) {
                String extName = FileNameUtil.extName(file);
                String pName = ShellFileUtil.parent(file);
                String fName = ShellFileUtil.name(file);
                if (StringUtil.equalsAnyIgnoreCase(extName, "7z", "rar", "zip")) {
                    return this.client.exec("cd " + pName + " && 7z x " + fName + " -y");
                }
                return this.client.exec("cd " + pName + " && tar -axof " + fName + " --owner=$(whoami) --group=$(id -gn)");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取时区
     *
     * @return 时区
     */
    public String timezone() {
        try {
            if (this.client.isWindows()) {
                return this.client.exec("tzutil /g");
            } else if (this.client.isLinux()) {
                String output = this.client.exec("cat /etc/timezone");
                if (StringUtil.containsIgnoreCase(output, "Is a directory")) {
                    output = this.client.exec("cat /etc/timezone/timezone");
                }
                if (StringUtil.isNotBlank(output) && !StringUtil.containsIgnoreCase(output, "No such file")) {
                    return output;
                }
            } else if (this.client.isUnix()) {
                return this.client.exec("cat /var/db/zoneinfo 2>/dev/null");
            }
            String output = this.client.exec("ls -l /etc/localtime");
            String[] lines = output.split("->");
            if (lines.length == 2) {
                String time = lines[1].trim().split("zoneinfo")[1];
                return time.substring(1);
            }
            return "N/A";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "N/A";
    }

    /**
     * 获取区域
     *
     * @return 区域
     */
    public String locale() {
        try {
            if (this.client.isWindows()) {
                String output = this.client.exec("chcp");
                return ShellUtil.getCharsetFromChcp(output);
            } else {
                return this.client.exec("echo $LANG");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "N/A";
    }

    /**
     * 持久化命令
     */
    public void persistentCommand() {
        if (this.client.isLinux() || this.client.isUnix()) {
            this.client.exec("history -a");
        }
    }

    /**
     * 获取命令历史
     *
     * @param limit 限制数量
     * @param kw 过滤关键字
     * @return 命令历史
     */
    public List<String> history(Integer limit, String kw) {
        try {
            String shellName = this.client.getShellName();
            if (shellName == null) {
                return null;
            }
            String result = null;
            if (this.client.isMacos() || this.client.isLinux() || this.client.isUnix()) {
                result = this.client.exec("cat ~/." + shellName + "_history");
            }
            if (result != null) {
                List<String> list = result.lines().collect(Collectors.toList());
                if (StringUtil.isNotBlank(kw)) {
                    list = list.parallelStream().filter(line -> StringUtil.containsIgnoreCase(line, kw)).collect(Collectors.toList());
                }
                if (limit != null) {
                    long count = list.size();
                    long skip = Math.max(0, count - limit);
                    list = list.parallelStream().skip(skip).collect(Collectors.toList());
                }
                return list.reversed();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 获取终端类型
     *
     * @return 终端类型
     */
    public String getShellType() {
        String shellType;
        if (this.client.isUnix() || this.client.isLinux() || this.client.isMacos()) {
            shellType = this.client.exec("echo $SHELL");
        } else {
            shellType = this.client.exec("echo %ComSpec%");
        }
        return shellType;
    }

    @Override
    public void close() throws Exception {
        this.disk = null;
        this.client = null;
        this.network = null;
    }
}
