package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHUtil;

/**
 * 验证 PATH 注入不会改变 Exec 命令语义，并覆盖 cmd、PowerShell、POSIX 与 csh。
 */
public final class ShellJSchExecCommandRegression {

    private ShellJSchExecCommandRegression() {
    }

    public static void main(String[] args) {
        try {
            run();
            System.out.println("JSch exec command regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void run() throws Exception {
        int hostIndex = Integer.getInteger("test.exec.command.host.index", 1);
        ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(hostIndex);
        System.out.println("Exec command target: " + host);
        try (ShellSSHClient client = new ShellSSHClient(host.newConnect())) {
            client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(client);
            client.start(15000);
            String os = client.isWindows() ? "Windows" : client.exec("uname -s");
            System.out.println("Exec command shell: os=" + os + ", shell=" + client.getShellType());
            requireEquals("exec-command-ok", client.exec("echo exec-command-ok"), "basic command");

            if (client.isWindows()) {
                verifyWindowsCommand(client);
            } else {
                verifyUnixCommand(client);
            }
            verifyPathInjection(client);
            verifyRepeatedExecution(client);
        }
    }

    private static void verifyUnixCommand(ShellSSHClient client) throws Exception {
        requireEquals("exec:a b", client.exec("printf '%s' 'exec:a b'"), "quoted argument");
        requireEquals("EXEC:A B", client.exec("printf '%s' 'exec:a b' | tr a-z A-Z"), "pipeline");
        requireEquals("exec;ok", client.exec("printf '%s' 'exec;ok'"), "semicolon argument");
        requireEquals("/tmp", client.exec("cd /tmp && pwd"), "shell builtin and chaining");
        requireEquals("it's ok", client.exec("printf '%s' \"it's ok\""), "single quote in command");

        String csh = client.exec("command -v csh");
        if (csh != null && !csh.isBlank() && !csh.toLowerCase().contains("not found")) {
            String cshPath = "/tmp/easyshell-csh'quote";
            String wrapper = "setenv PATH " + ShellSSHUtil.quoteCommand(cshPath)
                    + ":\"$PATH\"; printf '%s' \"$PATH\"";
            String path = client.exec("csh -c " + ShellSSHUtil.quoteCommand(wrapper));
            require(path != null && path.startsWith(cshPath + ":"),
                    "csh compatibility wrapper lost PATH: " + path);
        }
    }

    private static void verifyWindowsCommand(ShellSSHClient client) throws Exception {
        if (client.isCmdType()) {
            requireEquals("cmd-before\ncmd-after", client.exec("echo cmd-before&echo cmd-after"), "cmd chaining");
            requireEquals("\"cmd&ok\"", client.exec("echo \"cmd&ok\""), "cmd quoted ampersand");
            String powerShell = client.exec("powershell -NoProfile -NonInteractive -Command "
                    + "\"try { $env:PATH='C:/Windows/System32' + ';' + $env:PATH } catch {}; "
                    + "Write-Output 'powershell-ok'\"");
            requireEquals("powershell-ok", powerShell, "PowerShell wrapper");
            verifyWindowsGitBash(client);
        } else if (client.isPowershellType()) {
            requireEquals("ps-before\nps-after",
                    client.exec("Write-Output 'ps-before'; Write-Output 'ps-after'"), "PowerShell chaining");
            requireEquals("a;b", client.exec("Write-Output 'a;b'"), "PowerShell quoted semicolon");
        } else if (client.isPosixShellType()) {
            verifyUnixCommand(client);
        }
    }

    private static void verifyWindowsGitBash(ShellSSHClient client) {
        String exists = client.exec("if exist \"C:\\Program Files\\Git\\bin\\bash.exe\" (echo yes)");
        if (!"yes".equals(exists)) {
            return;
        }
        String posixPath = client.getExportPath().replace(';', ':');
        String script = "PATH='" + posixPath + "':$PATH; export PATH; env";
        String output = client.exec("\"C:\\Program Files\\Git\\bin\\bash.exe\" -c \"" + script + "\"");
        String path = output == null ? null : output.lines()
                .filter(line -> line.startsWith("PATH="))
                .findFirst()
                .orElse(null);
        require(path != null && path.startsWith("PATH=" + posixPath + ":"),
                "Git Bash wrapper lost PATH: " + path);
    }

    private static void verifyPathInjection(ShellSSHClient client) {
        String configured = client.getExportPath();
        require(configured != null && !configured.isBlank(), "configured export path is blank");
        String path = null;
        String separator = null;
        if (client.isWindows() && client.isCmdType()) {
            path = client.exec("cmd /v:on /c \"echo !PATH!\"");
            separator = ";";
        } else if (client.isWindows() && client.isPowershellType()) {
            path = client.exec("Write-Output $env:PATH");
            separator = ";";
        } else if (client.isPosixShellType() || client.isCshType() || client.isTcshType()) {
            path = client.exec("printf '%s' \"$PATH\"");
            separator = ":";
        }
        if (path == null) {
            return;
        }
        String prefix = configured + separator;
        require(path.startsWith(prefix), "exec command did not prepend configured PATH: " + path);
        require(path.length() > prefix.length(), "exec command replaced remote PATH: " + path);
    }

    private static void verifyRepeatedExecution(ShellSSHClient client) {
        for (int i = 0; i < 3; i++) {
            requireEquals("exec-repeat-" + i, client.exec("echo exec-repeat-" + i), "repeated command");
        }
    }

    private static void requireEquals(String expected, String actual, String name) {
        String normalizedExpected = expected == null ? null : expected.replace("\r\n", "\n");
        String normalizedActual = actual == null ? null : actual.replace("\r\n", "\n");
        require(normalizedExpected != null && normalizedExpected.equals(normalizedActual),
                name + " mismatch: expected=" + normalizedExpected + ", actual=" + normalizedActual);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
