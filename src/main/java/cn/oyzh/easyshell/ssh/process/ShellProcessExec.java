package cn.oyzh.easyshell.ssh.process;

import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.server.ShellServerExec;
import cn.oyzh.easyshell.ssh.server.ShellServerNetwork;
import cn.oyzh.easyshell.util.ShellUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 进程执行器
 *
 * @author oyzh
 * @since 2025-03-29
 */
public class ShellProcessExec implements AutoCloseable {

    /**
     * Unix 进程列表。显式列格式避免 ps -e 把环境变量追加到命令列，-ww 防止命令被终端宽度截断。
     */
    private static final String UNIX_PS_COMMAND =
            "ps -axww -o user,pid,%cpu,%mem,vsz,rss,tty,stat,start,time,command";

    /**
     * ssh客户端
     */
    private volatile ShellSSHClient client;

    /**
     * 构造进程执行器
     *
     * @param client ssh客户端
     */
    public ShellProcessExec(ShellSSHClient client) {
        this.client = client;
    }

    @Override
    public synchronized void close() throws Exception {
        this.client = null;
        if (this.processAttr != null) {
            this.processAttr.clear();
            this.processAttr = null;
        }
        if (this.networksSpeed != null) {
            this.networksSpeed.clear();
            this.networksSpeed = null;
        }
    }

//    /**
//     * 启动时间，仅windows
//     */
//    private long upAt = -1;

    /**
     * 总内存，仅windows
     */
    private volatile long totalMemory = -1;

    /**
     * 进程属性，仅windows
     */
    private Map<String, ShellProcessAttr> processAttr;

    /**
     * 网络上下行
     */
    private Map<String, ShellServerNetwork> networksSpeed;

    /**
     * 获取进程信息
     *
     * @return 结果
     */
    public synchronized List<ShellProcessInfo> ps() {
        ShellSSHClient currentClient = this.client;
        if (currentClient == null) {
            return Collections.emptyList();
        }
        if (currentClient.isWindows()) {
            ShellServerExec serverExec = currentClient.serverExec();
            if (this.processAttr == null) {
                this.processAttr = new ConcurrentHashMap<>();
            }
            if (this.totalMemory == -1) {
                this.totalMemory = serverExec.totalMemory() * 1024 * 1024;
            }
            String command1 = "powershell -Command \" Get-Process | Select-Object ID,CPU,WorkingSet,StartTime,NAME | ConvertTo-Csv -NoTypeInformation\"";
            String command2 = "tasklist /V /FO csv";
            String output1 = currentClient.execWindows(command1);
            String output2 = currentClient.execWindows(command2);
            if (output1 == null || output2 == null) {
                return Collections.emptyList();
            }
            String[] arr = output2.split("\n");
            Set<String> activePids = new HashSet<>();
            for (int i = 1; i < arr.length; i++) {
                List<String> cols = ShellUtil.splitWindowsCommandResult(arr[i]);
                if (cols.size() < 9) {
                    continue;
                }
                String pid = cols.get(1);
                String stat = cols.get(5);
                String user = cols.get(6);
                if (user.contains("\\")) {
                    user = user.substring(user.lastIndexOf("\\") + 1);
                }
                activePids.add(pid);
                ShellProcessAttr attr = this.processAttr.get(pid);
                if (attr == null) {
                    attr = new ShellProcessAttr();
                    this.processAttr.put(pid, attr);
                }
                attr.setStat(stat);
                attr.setUser(user);
            }
            this.processAttr.keySet().retainAll(activePids);
            DownLatch latch = DownLatch.of(2);
            AtomicReference<List<ShellProcessInfo>> processInfos = new AtomicReference<>();
            ThreadUtil.start(() -> {
                try {
                    processInfos.set(ShellProcessParser.psForWindows(output1, this.processAttr, this.totalMemory));
                } finally {
                    latch.countDown();
                }
            });
            AtomicReference<Map<String, double[]>> networkSpeed = new AtomicReference<>();
            ThreadUtil.start(() -> {
                try {
                    networkSpeed.set(this.getNetworkUplinkAndDownlink_windows());
                } finally {
                    latch.countDown();
                }
            });
            latch.await();
            ShellProcessParser.parseNetworkSpeed_windows(processInfos.get(), networkSpeed.get());
            return processInfos.get();
        }
        if (currentClient.isLinux()) {
            String output = currentClient.exec(UNIX_PS_COMMAND);
            DownLatch latch = DownLatch.of(2);
            AtomicReference<List<ShellProcessInfo>> processInfos = new AtomicReference<>();
            ThreadUtil.start(() -> {
                try {
                    processInfos.set(ShellProcessParser.psForLinux(output));
                } finally {
                    latch.countDown();
                }
            });
            AtomicReference<Map<String, double[]>> networkSpeed = new AtomicReference<>();
            ThreadUtil.start(() -> {
                try {
                    networkSpeed.set(this.calcNetworkUplinkAndDownlink_linux());
                } finally {
                    latch.countDown();
                }
            });
            latch.await();
            ShellProcessParser.parseNetworkSpeed_linux(processInfos.get(), networkSpeed.get());
            return processInfos.get();
        }
        if (currentClient.isMacos()) {
            String output = currentClient.exec(UNIX_PS_COMMAND);
            DownLatch latch = DownLatch.of(2);
            AtomicReference<List<ShellProcessInfo>> processInfos = new AtomicReference<>();
            ThreadUtil.start(() -> {
                try {
                    processInfos.set(ShellProcessParser.psForLinux(output));
                } finally {
                    latch.countDown();
                }
            });
            AtomicReference<Map<String, double[]>> networkSpeed = new AtomicReference<>();
            ThreadUtil.start(() -> {
                try {
                    networkSpeed.set(this.calcNetworkUplinkAndDownlink_macos());
                } finally {
                    latch.countDown();
                }
            });
            latch.await();
            ShellProcessParser.parseNetworkSpeed_macos(processInfos.get(), networkSpeed.get());
            return processInfos.get();
        }
        if (currentClient.isOpenBSD()) {
            String output = currentClient.exec(UNIX_PS_COMMAND);
            return ShellProcessParser.psForUnix(output);
        }
        if (currentClient.isUnix()) {
            String output = currentClient.exec(UNIX_PS_COMMAND);
            return ShellProcessParser.psForUnix(output);
        }
        return Collections.emptyList();
    }

    /**
     * 杀死进程
     *
     * @param pid 进程id
     * @return 结果
     */
    public String kill(int pid) {
        ShellSSHClient currentClient = this.client;
        if (currentClient == null) {
            return null;
        }
        if (currentClient.isWindows()) {
            String command = "taskkill /PID " + pid;
            return currentClient.execWindows(command);
        }
        return currentClient.exec("kill " + pid);
    }

    /**
     * 强制杀死进程
     *
     * @param pid 进程id
     * @return 结果
     */
    public String forceKill(int pid) {
        ShellSSHClient currentClient = this.client;
        if (currentClient == null) {
            return null;
        }
        if (currentClient.isWindows()) {
            String command = "taskkill /F /PID " + pid;
            return currentClient.execWindows(command);
        }
        return currentClient.exec("kill -9 " + pid);
    }

    /**
     * 计算网络上下行 linux
     *
     * @return 网络上下行
     */
    public Map<String, double[]> calcNetworkUplinkAndDownlink_linux() {
        Map<String, double[]> networkSpeed = this.getNetworkUplinkAndDownlink_linux();
        for (Map.Entry<String, double[]> entry : networkSpeed.entrySet()) {
            String key = entry.getKey();
            ShellServerNetwork network;
            if (this.networksSpeed == null) {
                this.networksSpeed = new ConcurrentHashMap<>();
            }
            if (!this.networksSpeed.containsKey(key)) {
                network = new ShellServerNetwork();
                this.networksSpeed.put(key, network);
            } else {
                network = this.networksSpeed.get(key);
            }
            double[] value = network.calcSpeed(entry.getValue());
            networkSpeed.put(key, value);
        }
        return networkSpeed;
    }

    /**
     * 获取网络上下行 linux
     *
     * @return 网络上下行
     */
    public Map<String, double[]> getNetworkUplinkAndDownlink_linux() {
        Map<String, double[]> result = new HashMap<>();
        ShellSSHClient currentClient = this.client;
        if (currentClient == null) {
            return result;
        }
        try {
            String cmd = "sh -c 'for f in /proc/[0-9]*/net/dev; do pid=\"${f#/proc/}\"; pid=\"${pid%/net/dev}\"; awk -v pid=\"$pid\" \"NR>2 && !/^lo:/{rx+=\\$2; tx+=\\$10} END{if(rx+tx>0) print pid, rx, tx}\" \"$f\"; done'";
            String output = currentClient.exec(cmd);
            if (output == null || output.isBlank()) {
                return result;
            }
            String[] lines = output.split("\n");
            for (String line : lines) {
                String[] cols = line.trim().split("\\s+");
                if (cols.length < 3) {
                    continue;
                }
                try {
                    String pid = cols[0];
                    double receive = Double.parseDouble(cols[1]);
                    double send = Double.parseDouble(cols[2]);
                    result.put(pid, new double[]{send, receive});
                } catch (NumberFormatException ignored) {
                }
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * 计算网络上下行 macos
     *
     * @return 网络上下行
     */
    public Map<String, double[]> calcNetworkUplinkAndDownlink_macos() {
        Map<String, double[]> networkSpeed = this.getNetworkUplinkAndDownlink_macos();
        for (Map.Entry<String, double[]> entry : networkSpeed.entrySet()) {
            String key = entry.getKey();
            ShellServerNetwork network;
            if (this.networksSpeed == null) {
                this.networksSpeed = new ConcurrentHashMap<>();
            }
            if (!this.networksSpeed.containsKey(key)) {
                network = new ShellServerNetwork();
                this.networksSpeed.put(key, network);
            } else {
                network = this.networksSpeed.get(key);
            }
            double[] value = network.calcSpeed(entry.getValue());
            networkSpeed.put(key, value);
        }
        return networkSpeed;
    }

    /**
     * 获取网络上下行 macos
     *
     * @return 网络上下行
     */
    public Map<String, double[]> getNetworkUplinkAndDownlink_macos() {
        Map<String, double[]> result = new HashMap<>();
        ShellSSHClient currentClient = this.client;
        if (currentClient == null) {
            return result;
        }
        try {
            String output = currentClient.exec("nettop -P -x -l 1 | awk '{print $2, $4, $5}' | column -t");
            if (output == null) {
                return result;
            }
            String[] lines = output.split("\n");
            boolean first = true;
            for (String line : lines) {
                if (first) {
                    first = false;
                    continue;
                }
                String[] cols = line.split("\\s+");
                String col = cols[0];
                if (!RegexUtil.isNumber(cols[1]) || !RegexUtil.isNumber(cols[2])) {
                    continue;
                }
                double receive = Double.parseDouble(cols[1]);
                double send = Double.parseDouble(cols[2]);
                result.put(col, new double[]{send, receive});
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * 获取网络上下行 windows
     *
     * @return 网络上下行
     */
    public Map<String, double[]> getNetworkUplinkAndDownlink_windows() {
        Map<String, double[]> result = new HashMap<>();
        ShellSSHClient currentClient = this.client;
        if (currentClient == null) {
            return result;
        }
        try {
            String cmd = "powershell -Command \"Get-Counter '\\Process(*)\\IO Read Bytes/sec','\\Process(*)\\IO Write Bytes/sec' -EA 0 | % { $_.CounterSamples | ? { $_.InstanceName -notmatch '^(_total|idle)$' -and $_.CookedValue -gt 0 } } | group InstanceName | % { $p=$_.Name; $r=($_.Group|? Path -like '*read*').CookedValue; $w=($_.Group|? Path -like '*write*').CookedValue; $pidObj=Get-Process |?{$_.ProcessName -eq $p -or $_.ProcessName -like '$p#*'} | select -First 1; [PSCustomObject]@{'Process'=$p;'PID'=if($pidObj){$pidObj.Id}else{'N/A'};'Read_B/s'=[int]$r;'Write_B/s'=[int]$w} } | sort 'Read_B/s' -Desc | select -First 15 | ft -AutoSize\"";
            String output = currentClient.execWindows(cmd);
            if (output == null) {
                return result;
            }
            String[] lines = output.split("\n");
            boolean start = false;
            for (String line : lines) {
                if (line.startsWith("-----")) {
                    start = true;
                    continue;
                }
                if (!start) {
                    continue;
                }
                String[] cols = line.split("\\s+");
                if (cols.length == 0) {
                    break;
                }
                String col = cols[1];
                double receive = Double.parseDouble(cols[2]);
                double send = Double.parseDouble(cols[3]);
                result.put(col, new double[]{send / 1024d, receive / 1024d});
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
