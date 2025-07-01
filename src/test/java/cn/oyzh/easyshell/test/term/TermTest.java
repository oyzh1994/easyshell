package cn.oyzh.easyshell.test.term;

import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.easyshell.ssh2.ShellSSHUtil;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class TermTest {

    @Test
    public void test1() {
        String sockFile = RuntimeUtil.execForStr("bash", "-c", "ls -l $SSH_AUTH_SOCK");
        String sockFile1 = RuntimeUtil.execForStr("zsh", "-c", "ls -l $SSH_AUTH_SOCK");
        String sockFile2 = RuntimeUtil.execForStr("zsh", "-c", "echo $SSH_AUTH_SOCK && ssh-add -l");
        String sockFile3 = RuntimeUtil.execForStr("zsh", "-c", "env");
        String sockFile4 = RuntimeUtil.execForStr("zsh", "-c", "ssh-add -l");
        String sockFile5 = RuntimeUtil.execForStr("sh", "-c", "echo -n $SSH_AUTH_SOCK");
        String sockFile6 = RuntimeUtil.execForStr("sh", "-c", "launchctl getenv SSH_AUTH_SOCK");

        System.out.println(sockFile);
        System.out.println(sockFile1);
        System.out.println(sockFile2);
        System.out.println(sockFile3);
        System.out.println(sockFile4);
        System.out.println(sockFile5);
        System.out.println(sockFile6);
        System.out.println("---"+RuntimeUtil.execForStr("sh", "-c", "echo $SSH_AGENT_PID"));
    }

    @Test
    public void test2() {
        String sockFile = ShellSSHUtil.getSSHAgentSockFile();
        System.out.println(sockFile);
    }

    @Test
    public void test3() throws Exception {
        String sockFile = getSSHAuthSock();
        System.out.println(sockFile);
    }

    @Test
    public void test4() throws Exception {
        String sockFile = findSSHAgentSocketByPattern();
        System.out.println(sockFile);
    }

    @Test
    public void test5() throws Exception {
        String sockFile = ShellSSHUtil.getSSHAgentSockFile();
        System.out.println(sockFile);
    }

    public static String getSSHAuthSock() throws Exception {
        // Step 1: 获取 ssh-agent 的 PID
        ProcessBuilder pb = new ProcessBuilder("pgrep", "-f", "ssh-agent");
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String pid = reader.readLine();

        if (pid == null || pid.trim().isEmpty()) {
            throw new RuntimeException("ssh-agent is not running.");
        }

        // Step 2: 构造 /proc/<pid>/fd 目录路径（macOS 兼容）
        File fdDir = new File("/dev/fd");
        if (!fdDir.exists()) {
            fdDir = new File("/proc/" + pid.trim() + "/fd"); // Linux only
        }

        if (!fdDir.exists()) {
            throw new RuntimeException("Could not find file descriptors for ssh-agent.");
        }

        // Step 3: 列出所有软链接，寻找指向 ssh-xxx 目录的 socket
        ProcessBuilder lsofPb = new ProcessBuilder("lsof", "-p", pid.trim());
        Process lsofProcess = lsofPb.start();
        BufferedReader lsofReader = new BufferedReader(new InputStreamReader(lsofProcess.getInputStream()));

        String line;
        while ((line = lsofReader.readLine()) != null) {
            if (line.contains("IPv4") && line.contains("TCP") && line.contains("->")) continue;
            if (line.contains("unix") && line.contains("socket")) {
                String[] parts = line.split("\\s+");
                if (parts.length > 8) {
                    String path = parts[8];
                    if (path.contains("/ssh-") && path.contains("agent.")) {
                        System.out.println("Found SSH_AUTH_SOCK path: " + path);
                        return path;
                    }
                }
            }
        }

        throw new RuntimeException("Could not find valid SSH_AUTH_SOCK socket path.");
    }

    public static String findSSHAgentSocketByPattern() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("find", "/tmp/ssh-*/", "-name", "agent.*");
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String path = reader.readLine();

        int exitCode = process.waitFor();
        if (exitCode != 0 || path == null || path.isEmpty()) {
            throw new RuntimeException("No SSH agent socket found in /tmp/ssh-*/");
        }

        return path.trim();
    }

}
