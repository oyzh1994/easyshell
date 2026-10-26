package cn.oyzh.easyshell.ssh;

import cn.oyzh.easyshell.ssh.ShellExecCommandResolver.Family;
import org.junit.Assume;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Exec PATH 注入的命令包装边界测试。
 */
public class ShellExecCommandResolverTest {

    @Test
    public void posixWrapperPreservesCommandSemantics() throws Exception {
        String command = "cd /tmp; printf '%s' \"path-test:a b\" | tr a-z A-Z";
        String resolved = ShellExecCommandResolver.resolve(command, "/definitely/missing:/bin", Family.POSIX);
        assertEquals("PATH-TEST:A B", execute("/bin/sh", "-c", resolved));
    }

    @Test
    public void posixWrapperQuotesPathAndKeepsRemotePath() throws Exception {
        String resolved = ShellExecCommandResolver.resolve(
                "printf '%s' \"$PATH\"", "/tmp/it's path:/bin", Family.POSIX);
        String output = execute("/bin/sh", "-c", resolved);
        assertTrue(output, output.startsWith("/tmp/it's path:/bin:"));
        assertTrue(output, output.length() > "/tmp/it's path:/bin:".length());
    }

    @Test
    public void cshWrapperUsesNativeSetenvAndExtendsPath() {
        String resolved = ShellExecCommandResolver.resolve(
                "echo ok", "/custom/bin", Family.CSH);
        assertEquals("setenv PATH '/custom/bin':\"$PATH\"; echo ok", resolved);
    }

    @Test
    public void cshWrapperExecutesInCsh() throws Exception {
        Assume.assumeTrue(Files.isExecutable(Path.of("/bin/csh")));
        String resolved = ShellExecCommandResolver.resolve(
                "printf '%s' \"$PATH\"", "/tmp/exec csh", Family.CSH);
        String output = execute("/bin/csh", "-f", "-c", resolved);
        assertTrue(output, output.startsWith("/tmp/exec csh:"));
        assertTrue(output, output.length() > "/tmp/exec csh:".length());
    }

    @Test
    public void cmdWrapperIsStableForCommonAndQuotedCommands() {
        String command = "echo \"a&b\" & echo second";
        String resolved = ShellExecCommandResolver.resolve(command, "C:/Windows/System32;C:/Program Files", Family.CMD);
        assertEquals("set \"PATH=C:/Windows/System32;C:/Program Files;%PATH%\" & " + command, resolved);
    }

    @Test
    public void cmdUnsafePathFallsBackToOriginalCommand() {
        String command = "echo safe";
        assertEquals(command, ShellExecCommandResolver.resolve(command, "C:/bad\"path", Family.CMD));
        assertEquals(command, ShellExecCommandResolver.resolve(command, "C:/bad%path", Family.CMD));
        assertEquals(command, ShellExecCommandResolver.resolve(command, "C:/bad!path", Family.CMD));
        assertEquals(command, ShellExecCommandResolver.resolve(command, "C:/bad\npath", Family.CMD));
    }

    @Test
    public void powershellWrapperEscapesSingleQuote() {
        String command = "Write-Output 'still runs'";
        String resolved = ShellExecCommandResolver.resolve(command, "C:/Users/O'Brien/bin", Family.POWERSHELL);
        assertTrue(resolved.startsWith("try { $env:PATH='C:/Users/O''Brien/bin' + ';' + $env:PATH } catch {}; "));
        assertTrue(resolved.endsWith(command));
    }

    @Test
    public void windowsPosixWrapperExportsMsysVariables() {
        String command = "printf '%s' native";
        String resolved = ShellExecCommandResolver.resolve(
                command, "/c/Windows/System32:/c/Program Files", Family.WINDOWS_POSIX);
        assertTrue(resolved.contains("PATH='/c/Windows/System32:/c/Program Files':\"$PATH\"; export PATH; "));
        assertTrue(resolved.contains("export MSYS2_ARG_CONV_EXCL='*' MSYS_NO_PATHCONV=1; "));
        assertTrue(resolved.endsWith(command));
    }

    @Test
    public void powershellUnsafePathFallsBackToOriginalCommand() {
        String command = "Write-Output 'safe'";
        assertEquals(command, ShellExecCommandResolver.resolve(command, "C:/bad\rpath", Family.POWERSHELL));
        assertEquals(command, ShellExecCommandResolver.resolve(command, "C:/bad\npath", Family.POWERSHELL));
    }

    @Test
    public void unsupportedShellAndBlankPathDoNotChangeCommand() {
        String command = "echo untouched";
        assertEquals(command, ShellExecCommandResolver.resolve(command, "/custom/bin", Family.UNSUPPORTED));
        assertEquals(command, ShellExecCommandResolver.resolve(command, " ", Family.POSIX));
        assertEquals(command, ShellExecCommandResolver.resolve(command, null, Family.CMD));
    }

    @Test
    public void overlongWindowsWrapperFallsBackToOriginalCommand() {
        String command = "echo safe";
        String path = "C:/" + "x".repeat(9000);
        assertEquals(command, ShellExecCommandResolver.resolve(command, path, Family.CMD));
        assertEquals(command, ShellExecCommandResolver.resolve(command, path, Family.POWERSHELL));
        assertEquals(command, ShellExecCommandResolver.resolve(command, path, Family.WINDOWS_POSIX));
    }

    @Test
    public void cshHistoryCharacterFallsBackToOriginalCommand() {
        String command = "echo safe";
        assertEquals(command, ShellExecCommandResolver.resolve(command, "/tmp/!bad", Family.CSH));
        assertFalse(ShellExecCommandResolver.resolve(command, "/tmp/good", Family.CSH).equals(command));
    }

    private static String execute(String... command) throws Exception {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, process.waitFor());
        return output;
    }
}
