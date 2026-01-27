package cn.oyzh.easyshell.ssh2.process;

import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.RegexUtil;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.ssh2.server.ShellServerExec;
import cn.oyzh.easyshell.ssh2.server.ShellServerNetwork;
import cn.oyzh.easyshell.util.ShellUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 进程执行器
 *
 * @author oyzh
 * @since 25/03/29
 */
public class ShellProcessExec implements AutoCloseable {

    private ShellSSHClient client;

    public ShellProcessExec(ShellSSHClient client) {
        this.client = client;
    }

    @Override
    public void close() throws Exception {
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
    private long totalMemory = -1;

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
    public List<ShellProcessInfo> ps() {
        if (this.client.isWindows()) {
            ShellServerExec serverExec = this.client.serverExec();
            if (this.processAttr == null) {
                this.processAttr = new HashMap<>();
            }
            if (this.totalMemory == -1) {
                this.totalMemory = serverExec.totalMemory() * 1024 * 1024;
            }
//            if (this.upAt == -1) {
//                String uptime = serverExec.uptime();
//                uptime = uptime.split("up at ")[1];
//                try {
//                    Date date = DateHelper.DATE_TIME_SIMPLE_FORMAT.parse(uptime);
//                    this.upAt = date.getTime();
//                } catch (ParseException ex) {
//                    ex.printStackTrace();
//                }
//            }
//            String command = "powershell -Command \"$os = Get-CimInstance -ClassName Win32_OperatingSystem; Get-CimInstance -ClassName Win32_Process | ForEach-Object { $user = if ($_.GetOwner().User) { $_.GetOwner().User } else { \\\"N/A\\\" }; $memUsage = ($_.WorkingSetSize / ($os.TotalVisibleMemorySize * 1024)) * 100; \\\"{0},{1},{2},{3:F2},{4},{5},{6}\\\" -f $user, $_.ProcessId, 0, $memUsage, $_.WorkingSetSize, $_.CreationDate, $_.Name }\"";
//            String command = "powershell -Command \"Get-Process | Select-Object @{Name=\\\"USER\\\";Expression={$_.UserName}},Id,@{Name=\\\"%CPU\\\";Expression={($_.CPU / (Get-Date - $_.StartTime).TotalSeconds) * 100}},@{Name=\\\"%MEM\\\";Expression={($_.WorkingSet / (Get-CimInstance -ClassName Win32_OperatingSystem).TotalVisibleMemorySize) * 100}},WorkingSet,StartTime,Name | ConvertTo-Csv -NoTypeInformation\"";
//            String command = "powershell -Command \"$os = Get-CimInstance -ClassName Win32_OperatingSystem; $processes = Get-CimInstance -ClassName Win32_Process; $sessionProcesses = Get-CimInstance -ClassName Win32_SessionProcess; $logonSessions = Get-CimInstance -ClassName Win32_LogonSession; foreach ($process in $processes) { $sessionProcess = $sessionProcesses | Where-Object { $_.ProcessId -eq $process.ProcessId }; if ($sessionProcess) { $logonSession = $logonSessions | Where-Object { $_.LogonId -eq $sessionProcess.SessionId }; if ($logonSession) { $user = ($logonSession | Invoke-CimMethod -Name GetUsername).User; } else { $user = \\\"N/A\\\" } } else { $user = \\\"N/A\\\" }; $memUsage = ($process.WorkingSetSize / ($os.TotalVisibleMemorySize * 1024)) * 100; \\\"{0},{1},{2},{3:F2},{4},{5},{6}\\\" -f $user, $process.ProcessId, 0, $memUsage, $process.WorkingSetSize, $process.CreationDate, $process.Name }\"";
//            String command = "powershell -Command \"$os = Get-CimInstance -ClassName Win32_OperatingSystem; Get-CimInstance -ClassName Win32_Process | ForEach-Object { $memUsage = ($_.WorkingSetSize / ($os.TotalVisibleMemorySize * 1024)) * 100; \\\"{0},{1},{2:F2},{3},{4},{5}\\\" -f $_.ProcessId, 0, $memUsage, $_.WorkingSetSize, $_.CreationDate, $_.Name }\"";
//            String command = "powershell -Command \"$os = Get-CimInstance -ClassName Win32_OperatingSystem; $processes = Get-CimInstance -ClassName Win32_Process; $logonSessions = Get-CimInstance -ClassName Win32_LogonSession | Select-Object LogonId,@{Name=\\\"User\\\";Expression={$_.GetUsername().User}}; $processes | ForEach-Object { $sessionProcesses = Get-CimInstance -ClassName Win32_SessionProcess | Where-Object { $_.ProcessId -eq $_.ProcessId }; if ($sessionProcesses) { $sessionId = $sessionProcesses.SessionId; $userInfo = $logonSessions | Where-Object { $_.LogonId -eq $sessionId }; if ($userInfo) { $user = $userInfo.User; } else { $user = \\\"N/A\\\" } } else { $user = \\\"N/A\\\" }; $memUsage = ($_.WorkingSetSize / ($os.TotalVisibleMemorySize * 1024)) * 100; \\\"{0},{1},{2},{3:F2},{4},{5},{6}\\\" -f $user, $_.ProcessId, 0, $memUsage, $_.WorkingSetSize, $_.CreationDate, $_.Name }\"";
//            String command = "powershell -Command \"$os = Get-WmiObject -Class Win32_OperatingSystem; Get-WmiObject -Class Win32_Process | ForEach-Object { $owner = $_.GetOwner(); $user = if ($owner.User) { $owner.User } else { \\\"N/A\\\" }; $memUsage = ($_.WorkingSetSize / ($os.TotalVisibleMemorySize * 1024)) * 100; \\\"{0},{1},{2},{3:F2},{4},{5},{6}\\\" -f $user, $_.ProcessId, 0, $memUsage, $_.WorkingSetSize, $_.CreationDate, $_.Name }\"";
            String command1 = "powershell -Command \" Get-Process | Select-Object ID,CPU,WorkingSet,StartTime,NAME | ConvertTo-Csv -NoTypeInformation\"";
            String command2 = "tasklist /V /FO csv";
            String output1 = this.client.exec(command1);
            String output2 = this.client.exec(command2);
            String[] arr = output2.split("\n");
            for (int i = 1; i < arr.length; i++) {
                List<String> cols = ShellUtil.splitWindowsCommandResult(arr[i]);
                String pid = cols.get(1);
                String stat = cols.get(5);
                String user = cols.get(6);
                if (user.contains("\\")) {
                    user = user.substring(user.lastIndexOf("\\") + 1);
                }
                ShellProcessAttr attr = this.processAttr.get(pid);
                if (attr == null) {
                    attr = new ShellProcessAttr();
                    this.processAttr.put(pid, attr);
                } else {
                    attr.setStat(stat);
                    attr.setUser(user);
                }
            }
//            return ShellProcessParser.psForWindows(output1, this.processAttr, this.totalMemory);
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
        } else if (this.client.isLinux()) {
            String output = this.client.exec("ps -auxe");
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
        } else if (this.client.isMacos()) {
            String output = this.client.exec("ps -axo user,pid,%cpu,%mem,vsz,rss,tty,stat,start,time,command");
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
        } else if (this.client.isUnix()) {
            String output = this.client.exec("ps -auxe");
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
        if (this.client.isWindows()) {
            String command = "taskkill /PID " + pid;
            return this.client.exec(command);
        }
        return this.client.exec("kill " + pid);
    }

    /**
     * 强制杀死进程
     *
     * @param pid 进程id
     * @return 结果
     */
    public String forceKill(int pid) {
        if (this.client.isWindows()) {
            String command = "taskkill /F /PID " + pid;
            return this.client.exec(command);
        }
        return this.client.exec("kill -9 " + pid);
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
                this.networksSpeed = new HashMap<>();
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
        try {
            String cmd = "sh -c 'for f in /proc/[0-9]*/net/dev; do pid=\"${f#/proc/}\"; pid=\"${pid%/net/dev}\"; awk -v pid=\"$pid\" \"NR>2 && !/^lo:/{rx+=\\$2; tx+=\\$10} END{if(rx+tx>0) printf \\\"%8s  RX: %-15s  TX: %-15s\\\\n\\\", pid, rx, tx}\" \"$f\"; done'";
            String output = this.client.exec(cmd);
            String[] lines = output.split("\n");
            for (String line : lines) {
                if(line.contains("cannot open file")){
                    continue;
                }
                try {
                    String[] cols = line.split("\\s+");
                    String col = cols[1];
                    double receive = Double.parseDouble(cols[3]);
                    double send = Double.parseDouble(cols[5]);
                    result.put(col, new double[]{send, receive});
                } catch (Exception ex) {
                    ex.printStackTrace();
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
                this.networksSpeed = new HashMap<>();
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
        try {
            String output = this.client.exec("nettop -P -x -l 1 | awk '{print $2, $4, $5}' | column -t");
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
        try {
            String cmd = "powershell -Command \"Get-Counter '\\Process(*)\\IO Read Bytes/sec','\\Process(*)\\IO Write Bytes/sec' -EA 0 | % { $_.CounterSamples | ? { $_.InstanceName -notmatch '^(_total|idle)$' -and $_.CookedValue -gt 0 } } | group InstanceName | % { $p=$_.Name; $r=($_.Group|? Path -like '*read*').CookedValue; $w=($_.Group|? Path -like '*write*').CookedValue; $pidObj=Get-Process |?{$_.ProcessName -eq $p -or $_.ProcessName -like '$p#*'} | select -First 1; [PSCustomObject]@{'Process'=$p;'PID'=if($pidObj){$pidObj.Id}else{'N/A'};'Read_B/s'=[int]$r;'Write_B/s'=[int]$w} } | sort 'Read_B/s' -Desc | select -First 15 | ft -AutoSize\"";
            String output = this.client.exec(cmd);
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
