package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.docker.ShellDockerCommit;
import cn.oyzh.easyshell.ssh.process.ShellProcessInfo;
import cn.oyzh.easyshell.ssh.server.ShellServerInfo;
import cn.oyzh.easyshell.ssh.server.ShellServerMonitor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Windows 远程 Git Bash、PowerShell 扩展能力回归。
 */
public final class ShellJSchWindowsShellCompatibilityRegression {

    private ShellJSchWindowsShellCompatibilityRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch Windows shell compatibility regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.host.index", 2);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("Windows compatibility target: " + host);
        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);
            verifyMetadata(client);
            verifyCommands(client);
            verifyServerInfo(client);
            verifyMonitor(client);
            verifyProcesses(client);
            verifyFileOperations(client);
            verifyDocker(client);
        }
    }

    private static void verifyMetadata(ShellSSHClient client) {
        probe("$MSYSTEM", () -> client.execMetadata("echo $MSYSTEM"));
        probe("$OSTYPE", () -> client.execMetadata("echo $OSTYPE"));
        probe("uname", () -> client.execMetadata("uname -s"));
        probe("$env:ComSpec", () -> client.execMetadata("echo $env:ComSpec"));
        probe("$SHELL", () -> client.execMetadata("echo $SHELL"));
        probe("$LANG", () -> client.execMetadata("echo $LANG"));
        probe("$USERPROFILE", () -> client.execMetadata("echo $USERPROFILE"));
        probe("%ComSpec%", () -> client.execMetadata("echo %ComSpec%"));
        probe("chcp", () -> client.execMetadata("chcp"));
        System.out.println("metadata: windows=" + client.isWindows() + ", shell=" + client.getShellType()
                + ", posix=" + client.isPosixShellType() + ", powershell=" + client.isPowershellType()
                + ", home=" + client.getUserHome() + ", charset=" + client.getRemoteCharset()
                + ", exportPath=" + client.getExportPath());
        require(client.isWindows(), "windows was not detected");
        require(client.isCmdType() || client.isPowershellType() || client.isWindowsPosixShellType(),
                "windows shell was not resolved: " + client.getShellType());
        require(notBlank(client.getUserHome()) && !client.getUserHome().contains("%"),
                "user home was not expanded: " + client.getUserHome());
        require(client.getUserHome().matches("^[A-Za-z]:\\\\.*"),
                "user home is not a Windows path: " + client.getUserHome());
        require(notBlank(client.getRemoteCharset()), "remote charset is blank");
        require(java.nio.charset.Charset.isSupported(client.getRemoteCharset()),
                "remote charset is unsupported: " + client.getRemoteCharset());
        if (client.isWindowsPosixShellType()) {
            require(client.getExportPath().startsWith("/c/"),
                    "Windows POSIX PATH was not converted: " + client.getExportPath());
            require(!client.getExportPath().contains("C:"),
                    "Windows drive colon remained in POSIX PATH: " + client.getExportPath());
        }
    }

    private static void verifyCommands(ShellSSHClient client) throws Exception {
        if (client.isPowershellType()) {
            requireEquals("windows-compat", client.exec("Write-Output 'windows-compat'"), "basic command");
            requireEquals("A|B", client.exec("Write-Output 'a|b' | ForEach-Object { $_.ToUpper() }"),
                    "pipeline");
            requireEquals("it's ok", client.exec("Write-Output \"it's ok\""), "quote argument");
            requireEquals("中文", client.exec("Write-Output ([char]0x4E2D + [char]0x6587)"), "output encoding");
            requireEquals("C:\\Windows", client.exec("Set-Location C:\\Windows; Write-Output (Get-Location).Path"),
                    "working directory command");
            String path = client.exec("Write-Output $env:PATH");
            require(path != null && path.startsWith(client.getExportPath() + ";"), "PATH injection: " + path);
            requireEquals("native", client.exec("cmd.exe /c echo native"), "native command");
        } else if (client.isWindowsPosixShellType()) {
            requireEquals("windows-compat", client.exec("printf '%s' windows-compat"), "basic command");
            requireEquals("WINDOWS-COMPAT", client.exec("printf '%s' windows-compat | tr a-z A-Z"), "pipeline");
            requireEquals("it's ok", client.exec("printf '%s' \"it's ok\""), "quote argument");
            requireEquals("中文", client.exec("printf '\\344\\270\\255\\346\\226\\207'"), "output encoding");
            requireEquals("/c/Windows", client.exec("cd /c/Windows && pwd"), "working directory command");
            String path = client.exec("printf '%s' \"$PATH\"");
            require(path != null && path.startsWith(client.getExportPath() + ":"), "PATH injection: " + path);
            requireEquals("1:*", client.exec("cd /c/Windows && printf '%s:%s' "
                    + "\"$MSYS_NO_PATHCONV\" \"$MSYS2_ARG_CONV_EXCL\""), "MSYS environment");
            requireEquals("native", client.exec("cmd.exe /c echo native"), "native command");
        } else {
            throw new IllegalStateException("unexpected Windows shell: " + client.getShellType());
        }
    }

    private static void verifyServerInfo(ShellSSHClient client) throws Exception {
        ShellServerInfo info = client.serverExec().info();
        System.out.println("server info: uname=" + info.getUname() + ", arch=" + info.getArch()
                + ", memoryMB=" + info.getTotalMemory() + ", timezone=" + info.getTimezone()
                + ", locale=" + info.getLocale() + ", uptime=" + info.getUptime()
                + ", ulimit=" + info.getUlimit() + ", shell=" + info.getShellName());
        require(notBlank(info.getUname()), "uname is blank");
        require(notBlank(info.getArch()), "arch is blank");
        require(info.getTotalMemory() > 0, "total memory is invalid: " + info.getTotalMemory());
        require(notBlank(info.getTimezone()) && !info.getTimezone().contains("->"),
                "timezone is invalid: " + info.getTimezone());
        require(notBlank(info.getLocale()) && !info.getLocale().equals("N/A"),
                "locale is unavailable: " + info.getLocale());
        require(notBlank(info.getUptime()) && !info.getUptime().equals("N/A"),
                "uptime is unavailable: " + info.getUptime());
        require(notBlank(info.getShellName()), "shell name is blank");
        String cpuInfo = client.sshExec().cpu_info();
        String memoryInfo = client.sshExec().memory_info();
        String networkInfo = client.sshExec().network_interface_info();
        String gpuInfo = client.sshExec().gpu_info();
        probe("cpu_info", () -> cpuInfo);
        probe("memory_info", () -> memoryInfo);
        probe("disk_info", client.sshExec()::disk_info);
        probe("wmic logicaldisk", () -> client.exec("wmic logicaldisk get name, size, freespace, volumeName"));
        String logicalDisks = client.execWindows("powershell -NoProfile -c \"Get-CimInstance Win32_LogicalDisk | Select-Object DeviceID,Size,FreeSpace,VolumeName | ConvertTo-Csv -NoTypeInformation\"");
        System.out.println("powershell logicaldisk: " + (logicalDisks == null ? "null"
                : logicalDisks.replace("\r", "<CR>").replace("\n", "<LF>")));
        probe("network_interface_info", () -> networkInfo);
        probe("nvidia-smi", () -> client.execWindows("nvidia-smi"));
        probe("Get-Command nvidia-smi", () -> client.execWindows("Get-Command nvidia-smi -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Source"));
        probe("Win32_VideoController", () -> client.execWindows("Get-CimInstance Win32_VideoController | Format-List Caption,Name,DriverVersion"));
        probe("gpu_info", () -> gpuInfo);
        require(!client.sshExec().disk_info().isEmpty(), "disk information is empty");
        require(notBlank(cpuInfo) && !cpuInfo.equals("N/A"), "cpu information is unavailable");
        require(notBlank(memoryInfo) && !memoryInfo.equals("N/A"), "memory information is unavailable");
        require(notBlank(networkInfo), "network information is blank");
        require(notBlank(gpuInfo) && !gpuInfo.equals("N/A")
                        && !gpuInfo.matches("^(Mon|Tue|Wed|Thu|Fri|Sat|Sun) .*"),
                "gpu information is unavailable: " + gpuInfo);
        require(!isCommandMissing(gpuInfo), "gpu command was not resolved: " + gpuInfo);
        require(notBlank(client.sshExec().cat_environment()), "environment information is blank");
        require(notBlank(client.sshExec().cat_hosts()), "hosts file is blank");
    }

    private static void verifyMonitor(ShellSSHClient client) throws Exception {
        client.serverExec().monitor(true);
        Thread.sleep(3200L);
        ShellServerMonitor monitor = client.serverExec().monitor(true);
        System.out.println("monitor: cpu=" + monitor.getCpuUsage() + ", memory=" + monitor.getMemoryUsage()
                + ", disk=" + monitor.getDiskReadSpeed() + "/" + monitor.getDiskWriteSpeed()
                + ", network=" + monitor.getNetworkReceiveSpeed() + "/" + monitor.getNetworkSendSpeed());
        requireRange("cpu", monitor.getCpuUsage());
        requireRange("memory", monitor.getMemoryUsage());
        require(monitor.getDiskReadSpeed() >= 0 && monitor.getDiskWriteSpeed() >= 0,
                "disk speed is invalid");
        require(monitor.getNetworkReceiveSpeed() >= 0 && monitor.getNetworkSendSpeed() >= 0,
                "network speed is invalid");
    }

    private static void verifyProcesses(ShellSSHClient client) throws Exception {
        List<ShellProcessInfo> processes = client.processExec().ps();
        Thread.sleep(1200L);
        List<ShellProcessInfo> second = client.processExec().ps();
        ShellProcessInfo process = second.stream().filter(item -> item.getPid() > 0).findFirst().orElse(null);
        System.out.println("process: firstCount=" + processes.size() + ", secondCount=" + second.size()
                + ", first=" + formatProcess(process));
        require(processes.size() > 20 && second.size() > 20, "process list is unexpectedly small");
        require(process != null, "process list is empty");
        require(notBlank(process.getCommand()), "process command is blank");
        boolean hasUser = second.stream().anyMatch(item -> notBlank(item.getUser())
                && !item.getUser().equals("N/A"));
        boolean hasMemory = second.stream().anyMatch(item -> item.getMemUsage() > 0);
        require(hasUser, "process user information is unavailable");
        require(hasMemory, "process memory usage is unavailable");
        for (ShellProcessInfo item : second) {
            requireRange("process cpu", item.getCpuUsage());
            requireRange("process memory", item.getMemUsage());
        }
    }

    private static void verifyFileOperations(ShellSSHClient client) throws Exception {
        ShellSFTPClient sftp = client.sftpClient();
        String workDir = sftp.workDir();
        System.out.println("sftp workDir: " + workDir);
        require(notBlank(workDir), "sftp work dir is blank");
        String root = ShellFileUtil.concat(workDir, "easyshell-windows-" + UUID.randomUUID());
        String source = ShellFileUtil.concat(root, "source file.txt");
        String copied = ShellFileUtil.concat(root, "copied file.txt");
        String moved = ShellFileUtil.concat(root, "moved file.txt");
        String appended = ShellFileUtil.concat(root, "appended file.txt");
        String content = "easyshell-windows-中文-" + UUID.randomUUID();
        String appendPrefix = "prefix-中文-" + UUID.randomUUID();
        Path downloaded = Files.createTempFile("easyshell-windows-", ".txt");
        try {
            sftp.createDir(root);
            sftp.put(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), source, null);
            require(sftp.exist(source), "sftp upload failed");
            requireEquals(content, client.sshExec().cat_file(source), "server cat file");
            requireEquals(1, sftp.lsFile(root).stream().filter(ShellSFTPFile::isNormal).count(),
                    "sftp list size");
            sftp.put(new ByteArrayInputStream(appendPrefix.getBytes(StandardCharsets.UTF_8)), appended, null);
            client.serverExec().copy(source, copied);
            require(sftp.exist(copied), "server copy did not create destination: " + copied);
            client.serverExec().move(copied, moved);
            require(!sftp.exist(copied) && sftp.exist(moved), "server move failed: " + moved);
            ShellSFTPFile file = sftp.fileInfo(moved);
            sftp.get(file, downloaded.toString());
            requireEquals(content, Files.readString(downloaded, StandardCharsets.UTF_8), "file content");
            client.sshExec().append_file(source, appended);
            String appendedContent = client.sshExec().cat_file(appended);
            require(appendedContent.contains(appendPrefix) && appendedContent.contains(content),
                    "server append file is incomplete: " + appendedContent);
            client.serverExec().forceDel(moved, true);
            client.serverExec().forceDel(appended, true);
            require(!sftp.exist(moved), "server delete did not remove file");
            require(!sftp.exist(appended), "server append file was not removed");
        } finally {
            try {
                client.serverExec().forceDel(root, false);
            } catch (Exception ignored) {
            }
            Files.deleteIfExists(downloaded);
        }
        System.out.println("file operations verified: copy=move=get=delete");
    }

    private static void verifyDocker(ShellSSHClient client) {
        try {
            verifyDockerCommandQuoting(client);
            String version = client.dockerExec().docker_version();
            if (version == null || version.isBlank() || isCommandMissing(version)) {
                System.out.println("docker: unavailable");
                return;
            }
            System.out.println("docker: " + oneLine(version));
            String info = client.dockerExec().docker_info();
            String containers = client.dockerExec().docker_ps();
            String images = client.dockerExec().docker_images();
            System.out.println("docker_info: " + oneLine(info));
            System.out.println("docker_ps: " + oneLine(containers));
            System.out.println("docker_images: " + oneLine(images));
            require(info != null && containers != null && images != null,
                    "docker API returned null");
            if (isDockerDaemonUnavailable(info) || isDockerDaemonUnavailable(containers)
                    || isDockerDaemonUnavailable(images)) {
                System.out.println("docker daemon: unavailable");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("docker probe failed", ex);
        }
    }

    private static void verifyDockerCommandQuoting(ShellSSHClient client) {
        ShellDockerCommit commit = new ShellDockerCommit();
        commit.setContainerId("container-id");
        commit.setComment("it's $HOME");
        String command = client.dockerExec().docker_commit_cmd(commit);
        if (client.isPowershellType()) {
            require(command.contains("-m 'it''s $HOME'"), "PowerShell docker quoting: " + command);
        } else if (client.isWindowsPosixShellType()) {
            require(command.contains("-m 'it'\"'\"'s $HOME'"), "Git Bash docker quoting: " + command);
        } else {
            require(command.contains("-m \"it's $HOME\""), "cmd docker quoting: " + command);
        }
        System.out.println("docker command quoting verified: " + command);
    }

    private static void probe(String name, Callable<?> action) {
        try {
            Object value = action.call();
            System.out.println(name + ": " + oneLine(String.valueOf(value)));
        } catch (Exception ex) {
            System.out.println(name + ": FAILED: " + ex.getClass().getSimpleName() + ": " + safeMessage(ex));
        }
    }

    private static String formatProcess(ShellProcessInfo process) {
        if (process == null) {
            return "none";
        }
        return "pid=" + process.getPid() + ", user=" + process.getUser() + ", command=" + process.getCommand()
                + ", cpu=" + process.getCpuUsage() + ", memory=" + process.getMemUsage();
    }

    private static boolean isCommandMissing(String output) {
        String value = output.toLowerCase();
        return value.contains("not recognized") || value.contains("not found")
                || value.contains("不是内部或外部命令") || value.contains("无法找到文件")
                || value.contains("找不到文件") || value.contains("commandnotfoundexception");
    }

    private static boolean isDockerDaemonUnavailable(String output) {
        if (output == null) {
            return false;
        }
        String value = output.toLowerCase();
        return value.contains("failed to connect to the docker api")
                || value.contains("is the docker daemon running")
                || value.contains("cannot connect to the docker daemon")
                || value.contains("cannot find the file specified");
    }

    private static String oneLine(String output) {
        if (output == null || output.isBlank()) {
            return "blank";
        }
        String value = output.lines().map(String::strip).filter(line -> !line.isBlank()).findFirst().orElse("blank");
        return value.length() > 240 ? value.substring(0, 240) + "..." : value;
    }

    private static String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return message == null ? "" : oneLine(message);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static void requireRange(String name, double value) {
        require(value >= 0 && value <= 100, name + " usage is out of range: " + value);
    }

    private static void requireEquals(Object expected, Object actual, String name) {
        String normalizedExpected = expected == null ? null : expected.toString().replace("\r\n", "\n");
        String normalizedActual = actual == null ? null : actual.toString().replace("\r\n", "\n");
        require(normalizedExpected != null && normalizedExpected.equals(normalizedActual),
                name + " mismatch: expected=" + normalizedExpected + ", actual=" + normalizedActual);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
