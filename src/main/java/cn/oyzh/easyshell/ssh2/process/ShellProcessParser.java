package cn.oyzh.easyshell.ssh2.process;


import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.util.ShellUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 进程解析器
 *
 * @author oyzh
 * @since 25/03/29
 */
public class ShellProcessParser {

    /**
     * 解析进程信息，linux
     *
     * @param output 输出
     * @return 结果
     */
    public static List<ShellProcessInfo> psForLinux(String output) {
        try {
            String[] lines = output.split("\n");
            List<ShellProcessInfo> list = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                String[] cols = line.split("\\s+");
                ShellProcessInfo info = new ShellProcessInfo();
                info.setUser(cols[0]);
                info.setPid(Integer.parseInt(cols[1]));
                info.setCpuUsage(Double.parseDouble(cols[2]));
                info.setMemUsage(Double.parseDouble(cols[3]));
                double rss = Double.parseDouble(cols[4]);
                info.setRss(NumberUtil.scale(rss / 1024, 2));
                info.setStat(cols[7]);
                info.setStart(cols[8]);
                info.setTime(cols[9]);
                info.setCommand(cols[10]);
                list.add(info);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 解析进程信息，unix
     *
     * @param output 输出
     * @return 结果
     */
    public static List<ShellProcessInfo> psForUnix(String output) {
        try {
            String[] lines = output.split("\n");
            List<ShellProcessInfo> list = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                String[] cols = line.split("\\s+");
                ShellProcessInfo info = new ShellProcessInfo();
                info.setUser(cols[0]);
                info.setPid(Integer.parseInt(cols[1]));
                info.setCpuUsage(Double.parseDouble(cols[2]));
                info.setMemUsage(Double.parseDouble(cols[3]));
                double rss = Double.parseDouble(cols[4]);
                info.setRss(NumberUtil.scale(rss / 1024, 2));
                info.setStat(cols[7]);
                info.setStart(cols[8]);
                info.setTime(cols[9]);
                info.setCommand(cols[10]);
                list.add(info);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 解析进程信息，macos
     *
     * @param output 输出
     * @return 结果
     */
    public static List<ShellProcessInfo> psForMacos(String output) {
        try {
            String[] lines = output.split("\n");
            List<ShellProcessInfo> list = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                String[] cols = line.split("\\s+");
                ShellProcessInfo info = new ShellProcessInfo();
                info.setUser(cols[0]);
                info.setPid(Integer.parseInt(cols[1]));
                info.setCpuUsage(Double.parseDouble(cols[2]));
                info.setMemUsage(Double.parseDouble(cols[3]));
                double rss = Double.parseDouble(cols[4]);
                info.setRss(NumberUtil.scale(rss / 1024, 2));
                info.setStat(cols[7]);
                info.setStart(cols[8]);
                info.setTime(cols[9]);
                info.setCommand(cols[10]);
                list.add(info);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

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
                    info.setStat(stat == null ? "N/A" : stat);
                    info.setUser(user == null ? "N/A" : user);
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
                    usage = NumberUtil.scale(usage / totalMemory, 2) * 100;
                    info.setMemUsage(usage);
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
     * 解析网络速度 linux
     *
     * @param infos 进程信息
     * @param speed 速度信息
     */
    public static void parseNetworkSpeed_linux(List<ShellProcessInfo> infos, Map<String, double[]> speed) {
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
        for (Map.Entry<String, double[]> entry : speed.entrySet()) {
            try {
                String[] arr = entry.getKey().split("\\.");
                if (arr.length >= 2 && RegexUtil.isNumber(arr[1])) {
                    int pid = Integer.parseInt(arr[1]);
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
     * 解析网络速度 windows
     *
     * @param infos 进程信息
     * @param speed 速度信息
     */
    public static void parseNetworkSpeed_windows(List<ShellProcessInfo> infos, Map<String, double[]> speed) {
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
