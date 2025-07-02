// package cn.oyzh.easyshell.ssh.process;
//
// import cn.oyzh.easyshell.ssh.server.ShellServerExec;
// import cn.oyzh.easyshell.ssh.ShellSSHClient;
// import cn.oyzh.easyshell.util.ShellUtil;
//
// import java.util.Collections;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// /**
//  * 进程执行器
//  *
//  * @author oyzh
//  * @since 25/03/29
//  */
// public class ShellProcessExec implements AutoCloseable {
//
//     private ShellSSHClient client;
//
//     public ShellProcessExec(ShellSSHClient client) {
//         this.client = client;
//     }
//
//     @Override
//     public void close() throws Exception {
//         this.client = null;
//     }
//
// //    /**
// //     * 启动时间，仅windows
// //     */
// //    private long upAt = -1;
//
//     /**
//      * 总内存，仅windows
//      */
//     private long totalMemory = -1;
//
//     /**
//      * 进程属性，仅windows
//      */
//     private Map<String, ShellProcessAttr> processAttr;
//
//     public List<ShellProcessInfo> ps() {
//         if (this.client.isWindows()) {
//             ShellServerExec serverExec = this.client.serverExec();
//             if (processAttr == null) {
//                 processAttr = new HashMap<>();
//             }
//             if (this.totalMemory == -1) {
//                 this.totalMemory = serverExec.totalMemory() * 1024 * 1024;
//             }
// //            if (this.upAt == -1) {
// //                String uptime = serverExec.uptime();
// //                uptime = uptime.split("up at ")[1];
// //                try {
// //                    Date date = DateHelper.DATE_TIME_SIMPLE_FORMAT.parse(uptime);
// //                    this.upAt = date.getTime();
// //                } catch (ParseException ex) {
// //                    ex.printStackTrace();
// //                }
// //            }
// //            String command = "powershell -Command \"$os = Get-CimInstance -ClassName Win32_OperatingSystem; Get-CimInstance -ClassName Win32_Process | ForEach-Object { $user = if ($_.GetOwner().User) { $_.GetOwner().User } else { \\\"N/A\\\" }; $memUsage = ($_.WorkingSetSize / ($os.TotalVisibleMemorySize * 1024)) * 100; \\\"{0},{1},{2},{3:F2},{4},{5},{6}\\\" -f $user, $_.ProcessId, 0, $memUsage, $_.WorkingSetSize, $_.CreationDate, $_.Name }\"";
// //            String command = "powershell -Command \"Get-Process | Select-Object @{Name=\\\"USER\\\";Expression={$_.UserName}},Id,@{Name=\\\"%CPU\\\";Expression={($_.CPU / (Get-Date - $_.StartTime).TotalSeconds) * 100}},@{Name=\\\"%MEM\\\";Expression={($_.WorkingSet / (Get-CimInstance -ClassName Win32_OperatingSystem).TotalVisibleMemorySize) * 100}},WorkingSet,StartTime,Name | ConvertTo-Csv -NoTypeInformation\"";
// //            String command = "powershell -Command \"$os = Get-CimInstance -ClassName Win32_OperatingSystem; $processes = Get-CimInstance -ClassName Win32_Process; $sessionProcesses = Get-CimInstance -ClassName Win32_SessionProcess; $logonSessions = Get-CimInstance -ClassName Win32_LogonSession; foreach ($process in $processes) { $sessionProcess = $sessionProcesses | Where-Object { $_.ProcessId -eq $process.ProcessId }; if ($sessionProcess) { $logonSession = $logonSessions | Where-Object { $_.LogonId -eq $sessionProcess.SessionId }; if ($logonSession) { $user = ($logonSession | Invoke-CimMethod -Name GetUsername).User; } else { $user = \\\"N/A\\\" } } else { $user = \\\"N/A\\\" }; $memUsage = ($process.WorkingSetSize / ($os.TotalVisibleMemorySize * 1024)) * 100; \\\"{0},{1},{2},{3:F2},{4},{5},{6}\\\" -f $user, $process.ProcessId, 0, $memUsage, $process.WorkingSetSize, $process.CreationDate, $process.Name }\"";
// //            String command = "powershell -Command \"$os = Get-CimInstance -ClassName Win32_OperatingSystem; Get-CimInstance -ClassName Win32_Process | ForEach-Object { $memUsage = ($_.WorkingSetSize / ($os.TotalVisibleMemorySize * 1024)) * 100; \\\"{0},{1},{2:F2},{3},{4},{5}\\\" -f $_.ProcessId, 0, $memUsage, $_.WorkingSetSize, $_.CreationDate, $_.Name }\"";
// //            String command = "powershell -Command \"$os = Get-CimInstance -ClassName Win32_OperatingSystem; $processes = Get-CimInstance -ClassName Win32_Process; $logonSessions = Get-CimInstance -ClassName Win32_LogonSession | Select-Object LogonId,@{Name=\\\"User\\\";Expression={$_.GetUsername().User}}; $processes | ForEach-Object { $sessionProcesses = Get-CimInstance -ClassName Win32_SessionProcess | Where-Object { $_.ProcessId -eq $_.ProcessId }; if ($sessionProcesses) { $sessionId = $sessionProcesses.SessionId; $userInfo = $logonSessions | Where-Object { $_.LogonId -eq $sessionId }; if ($userInfo) { $user = $userInfo.User; } else { $user = \\\"N/A\\\" } } else { $user = \\\"N/A\\\" }; $memUsage = ($_.WorkingSetSize / ($os.TotalVisibleMemorySize * 1024)) * 100; \\\"{0},{1},{2},{3:F2},{4},{5},{6}\\\" -f $user, $_.ProcessId, 0, $memUsage, $_.WorkingSetSize, $_.CreationDate, $_.Name }\"";
// //            String command = "powershell -Command \"$os = Get-WmiObject -Class Win32_OperatingSystem; Get-WmiObject -Class Win32_Process | ForEach-Object { $owner = $_.GetOwner(); $user = if ($owner.User) { $owner.User } else { \\\"N/A\\\" }; $memUsage = ($_.WorkingSetSize / ($os.TotalVisibleMemorySize * 1024)) * 100; \\\"{0},{1},{2},{3:F2},{4},{5},{6}\\\" -f $user, $_.ProcessId, 0, $memUsage, $_.WorkingSetSize, $_.CreationDate, $_.Name }\"";
//             String command1 = "powershell -Command \" Get-Process | Select-Object ID,CPU,WorkingSet,StartTime,NAME | ConvertTo-Csv -NoTypeInformation\"";
//             String command2 = "tasklist /V /FO csv";
//             String output1 = this.client.exec(command1);
//             String output2 = this.client.exec(command2);
//             String[] arr = output2.split("\n");
//             for (int i = 1; i < arr.length; i++) {
//                 List<String> cols = ShellUtil.splitWindowsCommandResult(arr[i]);
//                 String pid = cols.get(1);
//                 String stat = cols.get(5);
//                 String user = cols.get(6);
//                 if (user.contains("\\")) {
//                     user = user.substring(user.lastIndexOf("\\") + 1);
//                 }
//                 ShellProcessAttr attr = this.processAttr.get(pid);
//                 if (attr == null) {
//                     attr = new ShellProcessAttr();
//                     this.processAttr.put(pid, attr);
//                 } else {
//                     attr.setStat(stat);
//                     attr.setUser(user);
//                 }
//             }
//             return ShellProcessParser.psForWindows(output1, this.processAttr, this.totalMemory);
//         } else if (this.client.isLinux()) {
//             String output = this.client.exec("ps -auxe");
//             return ShellProcessParser.psForLinux(output);
//         } else if (this.client.isMacos()) {
//             String output = this.client.exec("ps -axo user,pid,%cpu,%mem,vsz,rss,tty,stat,start,time,command");
//             return ShellProcessParser.psForMacos(output);
//         } else if (this.client.isUnix()) {
//             String output = this.client.exec("ps -auxe");
//             return ShellProcessParser.psForUnix(output);
//         }
//         return Collections.emptyList();
//     }
//
//     public String kill(int pid) {
//         if (this.client.isWindows()) {
//             String command = "taskkill /PID " + pid;
//             return this.client.exec(command);
//         }
//         return this.client.exec("kill " + pid);
//     }
//
//     public String forceKill(int pid) {
//         if (this.client.isWindows()) {
//             String command = "taskkill /F /PID " + pid;
//             return this.client.exec(command);
//         }
//         return this.client.exec("kill -9 " + pid);
//     }
// }
