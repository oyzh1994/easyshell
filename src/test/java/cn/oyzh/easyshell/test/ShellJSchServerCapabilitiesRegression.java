package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.process.ShellProcessInfo;
import cn.oyzh.easyshell.ssh.server.ShellServerInfo;
import cn.oyzh.easyshell.ssh.server.ShellServerMonitor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * 验证 SSH 元数据、监控、进程、SFTP 和服务端文件操作组合能力。
 */
public final class ShellJSchServerCapabilitiesRegression {

    private ShellJSchServerCapabilitiesRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch server capabilities regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(Integer.getInteger("test.host.index", 1));
        System.out.println("Server capabilities target: " + host);
        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);
            String osType = verifyMetadata(client);
            verifyInfo(client.serverExec().info());
            verifyDetails(client, osType);
            // 网络速度需要使用两次累计值计算，首次采样用于建立基线。
            client.serverExec().monitor(true);
            Thread.sleep(3200L);
            verifyMonitor(client.serverExec().monitor(true));
            verifyProcesses(client.processExec().ps());
            verifyFileOperations(client);
        }
    }

    private static String verifyMetadata(ShellSSHClient client) {
        String osType = client.exec("uname -s");
        require(osType != null && !osType.isBlank(), "os type is blank");
        require(client.getShellType() != null && !client.getShellType().isBlank(), "shell type is blank");
        require(client.getUserHome() != null && !client.getUserHome().isBlank(), "user home is blank");
        String marker = "capability-" + UUID.randomUUID();
        require(marker.equals(client.exec("printf '%s' " + quote(marker))), "exec output mismatch");
        System.out.println("metadata verified: os=" + osType + ", shell=" + client.getShellType()
                + ", home=" + client.getUserHome());
        return osType;
    }

    private static void verifyInfo(ShellServerInfo info) {
        require(info != null, "server info is null");
        require(info.getUname() != null && !info.getUname().isBlank(), "uname is blank");
        require(info.getArch() != null && !info.getArch().isBlank(), "arch is blank");
        require(info.getTotalMemory() > 0, "total memory was not detected: " + info.getTotalMemory());
        require(info.getTimezone() != null && !info.getTimezone().isBlank(), "timezone is blank");
        require(!info.getTimezone().contains("->"), "timezone is a raw link: " + info.getTimezone());
        require(!info.getTimezone().contains("No such file"), "timezone is unavailable: " + info.getTimezone());
        System.out.println("server info verified: uname=" + info.getUname() + ", arch=" + info.getArch()
                + ", memoryMB=" + info.getTotalMemory() + ", timezone=" + info.getTimezone());
    }

    private static void verifyDetails(ShellSSHClient client, String osType) {
        if (!osType.equalsIgnoreCase("OpenBSD")) {
            return;
        }
        String cpu = client.sshExec().cpu_info();
        requireUsableInfo("cpu", cpu);
        require(cpu.contains("hw.model"), "OpenBSD cpu model is missing: " + cpu);

        String memory = client.sshExec().memory_info();
        requireUsableInfo("memory", memory);
        require(memory.contains("hw.physmem") || memory.contains("pages managed"),
                "OpenBSD memory information is missing: " + memory);

        String gpu = client.sshExec().gpu_info();
        requireInfoText("gpu", gpu);
        String display = client.exec("pcidump -v 2>/dev/null | grep 'Class: 03 Display'");
        boolean hasDisplay = display != null && !display.isBlank() && !display.toLowerCase().contains("not found");
        require(!hasDisplay || !gpu.equals("N/A"), "OpenBSD display device was found but gpu information is unavailable");
        System.out.println("OpenBSD details verified: cpu=" + cpu.replace('\n', ' ')
                + ", gpu=" + gpu.replace('\n', ' '));
    }

    private static void requireUsableInfo(String name, String output) {
        requireInfoText(name, output);
        require(!output.equals("N/A"), name + " information is unavailable");
    }

    private static void requireInfoText(String name, String output) {
        require(output != null && !output.isBlank(), name + " information is blank");
        require(!output.toLowerCase().contains("not found"), name + " command is unavailable: " + output);
        require(!output.toLowerCase().contains("invalid"), name + " command failed: " + output);
    }

    private static void verifyMonitor(ShellServerMonitor monitor) {
        require(monitor != null, "server monitor is null");
        requireRange("cpu", monitor.getCpuUsage());
        requireRange("memory", monitor.getMemoryUsage());
        require(monitor.getDiskReadSpeed() >= 0 || monitor.getDiskWriteSpeed() >= 0,
                "disk usage was not detected");
        require(monitor.getNetworkReceiveSpeed() >= 0 || monitor.getNetworkSendSpeed() >= 0,
                "network usage was not detected");
        System.out.println("monitor verified: cpu=" + monitor.getCpuUsage()
                + ", memory=" + monitor.getMemoryUsage()
                + ", disk=" + monitor.getDiskReadSpeed() + "/" + monitor.getDiskWriteSpeed()
                + ", network=" + monitor.getNetworkReceiveSpeed() + "/" + monitor.getNetworkSendSpeed());
    }

    private static void verifyProcesses(List<ShellProcessInfo> processes) {
        require(processes != null && !processes.isEmpty(), "process list is empty");
        ShellProcessInfo process = processes.stream().filter(item -> item.getPid() > 0).findFirst().orElse(null);
        require(process != null, "no process with a valid pid was found");
        require(process.getCommand() != null && !process.getCommand().isBlank(), "process command is blank");
        System.out.println("process verified: count=" + processes.size() + ", pid=" + process.getPid()
                + ", command=" + process.getCommand());
    }

    private static void verifyFileOperations(ShellSSHClient client) throws Exception {
        String root = "/tmp/easyshell-capability-" + UUID.randomUUID();
        String source = root + "/source file.txt";
        String copied = root + "/copied file.txt";
        String moved = root + "/moved file.txt";
        String content = "easyshell-capability-" + UUID.randomUUID();
        Path downloaded = Files.createTempFile("easyshell-capability-", ".txt");
        ShellSFTPClient sftp = client.sftpClient();
        try {
            sftp.createDir(root);
            sftp.put(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), source, null);
            requireEquals(1, sftp.lsFile(root).size(), "sftp list size");
            require(sftp.fileInfo(source) != null, "sftp file info is null");

            client.serverExec().copy(source, copied);
            require(sftp.exist(copied), "server copy did not create destination");
            client.serverExec().move(copied, moved);
            require(!sftp.exist(copied) && sftp.exist(moved), "server move did not update destination");

            ShellSFTPFile file = sftp.fileInfo(moved);
            sftp.get(file, downloaded.toString());
            requireEquals(content, Files.readString(downloaded, StandardCharsets.UTF_8), "file content");

            client.serverExec().forceDel(moved, true);
            require(!sftp.exist(moved), "server delete did not remove file");
            client.serverExec().forceDel(root, false);
            require(!sftp.exist(root), "server delete did not remove directory");
            System.out.println("server file operations verified: copy=move=get=delete");
        } finally {
            try {
                client.serverExec().forceDel(root, false);
            } catch (Exception ignored) {
            }
            Files.deleteIfExists(downloaded);
        }
    }

    private static void requireRange(String name, double value) {
        require(value >= 0 && value <= 100, name + " usage is out of range: " + value);
    }

    private static void requireEquals(Object expected, Object actual, String name) {
        require(expected.equals(actual), name + " mismatch: expected=" + expected + ", actual=" + actual);
    }

    private static String quote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
