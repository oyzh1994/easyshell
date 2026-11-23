package cn.oyzh.easyshell.ssh.process;


import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.ShellUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 进程解析器
 *
 * @author oyzh
 * @since 2025-03-29
 */
public class ShellProcessParser {

    /**
     * 解析进程信息，linux
     *
     * @param output 输出
     * @return 结果
     */
    public static List<ShellProcessInfo> psForLinux(String output) {
//        try {
//            String[] lines = output.split("\n");
//            List<ShellProcessInfo> list = new ArrayList<>();
//            for (int i = 1; i < lines.length; i++) {
//                String line = lines[i];
//                String[] cols = line.split("\\s+");
//                ShellProcessInfo info = new ShellProcessInfo();
//                info.setUser(cols[0]);
//                info.setPid(Integer.parseInt(cols[1]));
//                info.setCpuUsage(Double.parseDouble(cols[2]));
//                info.setMemUsage(Double.parseDouble(cols[3]));
//                double rss = Double.parseDouble(cols[4]);
//                info.setRss(NumberUtil.scale(rss / 1024, 2));
//                info.setStat(cols[7]);
//                info.setStart(cols[8]);
//                info.setTime(cols[9]);
//                info.setCommand(cols[10]);
//                list.add(info);
//            }
//            return list;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return Collections.emptyList();
        return psForUnix(output);
    }

    /**
     * 解析进程信息，unix
     *
     * @param output 输出
     * @return 结果
     */
    public static List<ShellProcessInfo> psForUnix(String output) {
        if (StringUtil.isBlank(output)) {
            return Collections.emptyList();
        }
        try {
            String[] lines = output.split("\n");
            List<ShellProcessInfo> list = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                String[] cols = line.trim().split("\\s+");
                if (cols.length < 11) {
                    continue;
                }
                ShellProcessInfo info = new ShellProcessInfo();
                info.setUser(cols[0]);
                info.setPid(Integer.parseInt(cols[1]));
                info.setCpuUsage(Double.parseDouble(cols[2]));
                info.setMemUsage(Double.parseDouble(cols[3]));
                double rss = Double.parseDouble(cols[5]);
                info.setRss(NumberUtil.scale(rss / 1024, 2));
                info.setStat(cols[7]);
                // Linux 的 start 可能是 "Oct 23" 两段，其它 Unix 通常只有一段。
                int startIndex = 8;
                int commandIndex = startIndex + 2;
                if (isMonth(cols[startIndex]) && cols.length > 9 && cols[9].matches("\\d{1,2}")) {
                    commandIndex++;
                }
                if (cols.length <= commandIndex) {
                    continue;
                }
                info.setStart(String.join(" ", Arrays.copyOfRange(cols, startIndex, commandIndex - 1)));
                info.setTime(cols[commandIndex - 1]);
                info.setCommand(String.join(" ", Arrays.copyOfRange(cols, commandIndex, cols.length)));
                list.add(info);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static boolean isMonth(String value) {
        return StringUtil.equalsAnyIgnoreCase(value,
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    }

//    /**
//     * 解析进程信息，macos
//     *
//     * @param output 输出
//     * @return 结果
//     */
//    public static List<ShellProcessInfo> psForMacos(String output) {
//        try {
//            String[] lines = output.split("\n");
//            List<ShellProcessInfo> list = new ArrayList<>();
//            for (int i = 1; i < lines.length; i++) {
//                String line = lines[i];
//                String[] cols = line.split("\\s+");
//                ShellProcessInfo info = new ShellProcessInfo();
//                info.setUser(cols[0]);
//                info.setPid(Integer.parseInt(cols[1]));
//                info.setCpuUsage(Double.parseDouble(cols[2]));
//                info.setMemUsage(Double.parseDouble(cols[3]));
//                double rss = Double.parseDouble(cols[4]);
//                info.setRss(NumberUtil.scale(rss / 1024, 2));
//                info.setStat(cols[7]);
//                info.setStart(cols[8]);
//                info.setTime(cols[9]);
//                info.setCommand(cols[10]);
//                list.add(info);
//            }
//            return list;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return Collections.emptyList();
//    }

//    public static List<ShellProcessInfo> psForWindows(String output) {
//        try {
//            String[] lines = output.split("\n");
//            List<ShellProcessInfo> list = new ArrayList<>();
//            for (int i = 0; i < lines.length; i++) {
//                String line = lines[i];
//                List<String> cols = List.of(line.split(","));
//                ShellProcessInfo info = new ShellProcessInfo();
//                info.setUser(cols.get(0));
//                info.setPid(Integer.parseInt(cols.get(1)));
//                String cpuUsage = cols.get(2);
//                if (!StringUtil.isBlank(cpuUsage)) {
//                    info.setCpuUsage(Double.parseDouble(cpuUsage));
//                }
//                String memUsage = cols.get(3);
//                if (!StringUtil.isBlank(memUsage)) {
//                    info.setMemUsage(Double.parseDouble(memUsage));
//                }
//                info.setStart(cols.get(5));
//                info.setCommand(cols.get(6));
//                list.add(info);
//            }
//            return list;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return Collections.emptyList();
//    }

    /**
     * 解析进程信息，windows
     *
     * @param output 输出
     * @return 结果
     */
    public static List<ShellProcessInfo> psForWindows(String output, Map<String, ShellProcessAttr> attrs, long totalMemory) {
        if (StringUtil.isBlank(output) || attrs == null || totalMemory <= 0) {
            return Collections.emptyList();
        }
        try {
            String[] lines = output.split("\n");
            List<ShellProcessInfo> list = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                List<String> cols = ShellUtil.splitWindowsCommandResult(line);
                String pid = cols.getFirst();
                ShellProcessInfo info = new ShellProcessInfo();
                ShellProcessAttr attr = attrs.get(pid);
                if (attr == null) {
                    info.setStat("N/A");
                    info.setUser("N/A");
                } else {
                    String stat = attr.getStat();
                    String user = attr.getUser();
                    info.setStat(StringUtil.isBlank(stat) ? "N/A" : stat);
                    info.setUser(StringUtil.isBlank(user) ? "N/A" : user);
                }
                info.setPid(Integer.parseInt(pid));
                String cpuUsage = cols.get(1);
                if (!StringUtil.isBlank(cpuUsage)) {
                    double usage = Double.parseDouble(cpuUsage);
                    usage = Math.abs(usage);
                    if (attr == null) {
                        attr = new ShellProcessAttr();
                        usage = attr.calcCpuUsage(usage);
                        attrs.put(pid, attr);
                        info.setCpuUsage(usage);
                    } else {
                        usage = attr.calcCpuUsage(usage);
                        usage = NumberUtil.scale(usage, 2) * 100;
                        info.setCpuUsage(usage);
                    }
                }
                String memUsage = cols.get(2);
                if (!StringUtil.isBlank(memUsage)) {
                    double usage = Double.parseDouble(memUsage);
                    usage = Math.abs(usage);
                    info.setRss(NumberUtil.scale(usage / 1024, 2));
                    info.setMemUsage(NumberUtil.scale(usage / totalMemory * 100, 2));
                }
                info.setStart(cols.get(3));
                info.setCommand(cols.get(4));
                list.add(info);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * ss 输出中的 pid
     */
    private static final Pattern SS_PID_PATTERN = Pattern.compile("pid=(\\d+)");

    /**
     * 解析 Linux 监听端口。优先解析 ss，兼容 netstat 输出。
     *
     * @param output 命令输出
     * @return pid -> 端口列表
     */
    public static Map<Integer, List<Integer>> parseLinuxPorts(String output) {
        if (StringUtil.isBlank(output)) {
            return Collections.emptyMap();
        }
        if (output.contains("users:((") || output.contains("Local Address:Port")) {
            return parseSsPorts(output);
        }
        return parseNetstatPorts(output);
    }

    /**
     * 解析 ss -ltnup 输出
     *
     * @param output 命令输出
     * @return pid -> 端口列表
     */
    public static Map<Integer, List<Integer>> parseSsPorts(String output) {
        Map<Integer, Set<Integer>> ports = new HashMap<>();
        for (String line : output.split("\\R")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("Netid") || line.startsWith("State")) {
                continue;
            }
            String[] cols = line.split("\\s+");
            if (cols.length < 6) {
                continue;
            }
            int port = parsePort(cols[4]);
            if (port <= 0) {
                continue;
            }
            Matcher matcher = SS_PID_PATTERN.matcher(line);
            while (matcher.find()) {
                addPort(ports, Integer.parseInt(matcher.group(1)), port);
            }
        }
        return toPortMap(ports);
    }

    /**
     * 解析 netstat 监听端口。兼容 Linux 与 Windows 的常见格式。
     *
     * @param output 命令输出
     * @return pid -> 端口列表
     */
    public static Map<Integer, List<Integer>> parseNetstatPorts(String output) {
        Map<Integer, Set<Integer>> ports = new HashMap<>();
        for (String line : output.split("\\R")) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            String upper = line.toUpperCase(Locale.ROOT);
            if (!upper.startsWith("TCP") && !upper.startsWith("UDP")) {
                continue;
            }
            // TCP 只保留监听状态，UDP 本身就是无连接的本地绑定端口
            if (upper.startsWith("TCP") && !upper.contains("LISTEN")) {
                continue;
            }
            String[] cols = line.split("\\s+");
            if (cols.length < 4) {
                continue;
            }
            String last = cols[cols.length - 1];
            int localIndex;
            int pid;
            if (last.matches("\\d+")) {
                // Windows: TCP local foreign state pid
                pid = Integer.parseInt(last);
                localIndex = 1;
            } else {
                // Unix: proto recv-q send-q local foreign state pid/program
                int slash = last.indexOf('/');
                if (slash <= 0 || !last.substring(0, slash).matches("\\d+")) {
                    continue;
                }
                pid = Integer.parseInt(last.substring(0, slash));
                localIndex = 3;
            }
            if (cols.length <= localIndex) {
                continue;
            }
            int port = parsePort(cols[localIndex]);
            if (port > 0 && pid > 0) {
                addPort(ports, pid, port);
            }
        }
        return toPortMap(ports);
    }

    /**
     * 解析 Windows 监听端口。优先解析 PowerShell 的结构化输出，兼容 netstat 输出。
     *
     * @param output 命令输出
     * @return pid -> 端口列表
     */
    public static Map<Integer, List<Integer>> parseWindowsPorts(String output) {
        if (StringUtil.isBlank(output)) {
            return Collections.emptyMap();
        }
        Map<Integer, Set<Integer>> ports = new HashMap<>();
        boolean structured = false;
        for (String line : output.split("\\R")) {
            String[] cols = line.trim().split(",");
            if (cols.length < 3 || !(cols[0].equals("T") || cols[0].equals("U"))) {
                continue;
            }
            if (!cols[1].matches("\\d+") || !cols[2].matches("\\d+")) {
                continue;
            }
            structured = true;
            addPort(ports, Integer.parseInt(cols[1]), Integer.parseInt(cols[2]));
        }
        if (structured) {
            return toPortMap(ports);
        }
        return parseNetstatPorts(output);
    }

    /**
     * 解析 lsof -Fpn 输出
     *
     * @param output 命令输出
     * @return pid -> 端口列表
     */
    public static Map<Integer, List<Integer>> parseLsofPorts(String output) {
        Map<Integer, Set<Integer>> ports = new HashMap<>();
        int pid = -1;
        for (String line : output.split("\\R")) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.charAt(0) == 'p') {
                String value = line.substring(1);
                pid = value.matches("\\d+") ? Integer.parseInt(value) : -1;
            } else if (line.charAt(0) == 'n' && pid > 0) {
                int port = parsePort(line.substring(1));
                if (port > 0) {
                    addPort(ports, pid, port);
                }
            }
        }
        return toPortMap(ports);
    }

    /**
     * 解析 BSD sockstat -l 输出
     *
     * @param output 命令输出
     * @return pid -> 端口列表
     */
    public static Map<Integer, List<Integer>> parseSockstatPorts(String output) {
        Map<Integer, Set<Integer>> ports = new HashMap<>();
        for (String line : output.split("\\R")) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("USER")) {
                continue;
            }
            String[] cols = line.split("\\s+");
            // USER COMMAND PID FD PROTO LOCAL_ADDRESS FOREIGN_ADDRESS
            if (cols.length < 6 || !cols[2].matches("\\d+")) {
                continue;
            }
            int port = parsePort(cols[5]);
            if (port > 0) {
                addPort(ports, Integer.parseInt(cols[2]), port);
            }
        }
        return toPortMap(ports);
    }

    /**
     * 将端口信息合并到进程列表
     *
     * @param infos      进程列表
     * @param portsByPid pid -> 端口列表
     */
    public static void parsePorts(List<ShellProcessInfo> infos, Map<Integer, List<Integer>> portsByPid) {
        if (infos == null || infos.isEmpty() || portsByPid == null || portsByPid.isEmpty()) {
            return;
        }
        Map<Integer, ShellProcessInfo> processByPid = new HashMap<>(infos.size() * 2);
        for (ShellProcessInfo info : infos) {
            processByPid.put(info.getPid(), info);
        }
        for (Map.Entry<Integer, List<Integer>> entry : portsByPid.entrySet()) {
            ShellProcessInfo info = processByPid.get(entry.getKey());
            if (info == null || entry.getValue().isEmpty()) {
                continue;
            }
            StringBuilder value = new StringBuilder();
            for (Integer port : entry.getValue()) {
                if (value.length() > 0) {
                    value.append(',');
                }
                value.append(port);
            }
            info.setPorts(value.toString());
        }
    }

    /**
     * 添加端口
     */
    private static void addPort(Map<Integer, Set<Integer>> ports, int pid, int port) {
        ports.computeIfAbsent(pid, key -> new TreeSet<>()).add(port);
    }

    /**
     * 转换端口映射
     */
    private static Map<Integer, List<Integer>> toPortMap(Map<Integer, Set<Integer>> ports) {
        Map<Integer, List<Integer>> result = new HashMap<>(ports.size());
        ports.forEach((pid, values) -> result.put(pid, new ArrayList<>(values)));
        return result;
    }

    /**
     * 从本地地址中解析端口
     */
    private static int parsePort(String address) {
        if (StringUtil.isBlank(address)) {
            return -1;
        }
        int index = address.lastIndexOf(':');
        if (index < 0 || index == address.length() - 1) {
            return -1;
        }
        String value = address.substring(index + 1);
        if (!value.matches("\\d+")) {
            return -1;
        }
        try {
            int port = Integer.parseInt(value);
            return port > 0 && port <= 65535 ? port : -1;
        } catch (NumberFormatException ignore) {
            return -1;
        }
    }

    /**
     * 解析网络速度 linux
     *
     * @param infos 进程信息
     * @param speed 速度信息
     */
    public static void parseNetworkSpeed_linux(List<ShellProcessInfo> infos, Map<String, double[]> speed) {
        if (infos == null || speed == null) {
            return;
        }
        for (Map.Entry<String, double[]> entry : speed.entrySet()) {
            try {
                int pid = Integer.parseInt(entry.getKey());
                double[] value = entry.getValue();
                Optional<ShellProcessInfo> optional = infos.parallelStream().filter(i -> i.getPid() == pid).findAny();
                if (optional.isPresent()) {
                    optional.get().setNetworkSend(value[0]);
                    optional.get().setNetworkRecv(value[1]);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 解析网络速度 macos
     *
     * @param infos 进程信息
     * @param speed 速度信息
     */
    public static void parseNetworkSpeed_macos(List<ShellProcessInfo> infos, Map<String, double[]> speed) {
        if (infos == null || speed == null) {
            return;
        }
        for (Map.Entry<String, double[]> entry : speed.entrySet()) {
            try {
                String key = entry.getKey();
                int dot = key.lastIndexOf('.');
                String pidValue = dot >= 0 && dot < key.length() - 1
                        ? key.substring(dot + 1) : key;
                if (RegexUtil.isNumber(pidValue)) {
                    int pid = Integer.parseInt(pidValue);
                    double[] value = entry.getValue();
                    Optional<ShellProcessInfo> optional = infos.parallelStream().filter(i -> i.getPid() == pid).findAny();
                    if (optional.isPresent()) {
                        optional.get().setNetworkSend(value[0]);
                        optional.get().setNetworkRecv(value[1]);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 解析 macOS nettop CSV 输出
     *
     * @param output nettop -P -x -L 1 -n 输出
     * @return 进程 -> {发送, 接收}
     */
    public static Map<String, double[]> parseNetworkSpeedOutputMacos(String output) {
        Map<String, double[]> result = new HashMap<>();
        if (StringUtil.isBlank(output)) {
            return result;
        }
        boolean first = true;
        for (String line : output.split("\\R")) {
            if (first) {
                first = false;
                continue;
            }
            List<String> cols = ShellUtil.splitWindowsCommandResult(line);
            // time,process,interface,state,bytes_in,bytes_out,...
            if (cols.size() < 6) {
                continue;
            }
            String process = cols.get(1);
            String receiveValue = cols.get(4);
            String sendValue = cols.get(5);
            if (StringUtil.isBlank(process)
                    || !RegexUtil.isNumber(receiveValue)
                    || !RegexUtil.isNumber(sendValue)) {
                continue;
            }
            double receive = Double.parseDouble(receiveValue);
            double send = Double.parseDouble(sendValue);
            result.put(process, new double[]{send, receive});
        }
        return result;
    }

    /**
     * 解析网络速度 windows
     *
     * @param infos 进程信息
     * @param speed 速度信息
     */
    public static void parseNetworkSpeed_windows(List<ShellProcessInfo> infos, Map<String, double[]> speed) {
        if (infos == null || speed == null) {
            return;
        }
        for (Map.Entry<String, double[]> entry : speed.entrySet()) {
            try {
                int pid = Integer.parseInt(entry.getKey());
                double[] value = entry.getValue();
                Optional<ShellProcessInfo> optional = infos.parallelStream().filter(i -> i.getPid() == pid).findAny();
                if (optional.isPresent()) {
                    optional.get().setNetworkSend(value[0]);
                    optional.get().setNetworkRecv(value[1]);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 格式化速度
     *
     * @param size  大小
     * @param scale 小数位
     * @return 结果
     */
    public static String formatSpeed(double size, Integer scale) {
        double result = size;
        result = Math.max(0, result);
        if (scale != null && scale >= 0) {
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(result) + "KB";
        }
        return result + "KB";
    }
}
