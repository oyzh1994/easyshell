package cn.oyzh.easyshell.local;

import cn.oyzh.common.system.OSUtil;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShellLocalCommandExecutorTest {

    @Test
    public void executesCommandAndCapturesOutput() {
        try (ShellLocalCommandExecutor executor = new ShellLocalCommandExecutor()) {
            ShellLocalCommandExecutor.CommandResult result = executor.execute("echo hello", null, 10);

            assertEquals(0, result.exitCode());
            assertTrue(result.stdout().contains("hello"));
            assertFalse(result.timedOut());
            assertFalse(result.interrupted());
        }
    }

    @Test
    public void capturesExitCodeAndStandardError() {
        String command = OSUtil.isWindows()
                ? "echo error 1>&2 & exit /b 7"
                : "echo error >&2; exit 7";
        try (ShellLocalCommandExecutor executor = new ShellLocalCommandExecutor()) {
            ShellLocalCommandExecutor.CommandResult result = executor.execute(command, null, 10);

            assertEquals(7, result.exitCode());
            assertTrue(result.stderr().contains("error"));
        }
    }

    @Test
    public void preservesQuotedCommandArguments() {
        String quote = String.valueOf((char) 34);
        try (ShellLocalCommandExecutor executor = new ShellLocalCommandExecutor()) {
            ShellLocalCommandExecutor.CommandResult result =
                    executor.execute("echo " + quote + "hello world" + quote, null, 10);

            assertEquals(0, result.exitCode());
            assertTrue(result.stdout().contains("hello world"));
        }
    }

    @Test
    public void usesSpecifiedWorkingDirectory() throws IOException {
        Path directory = Files.createTempDirectory("easyshell-local-tool-");
        try (ShellLocalCommandExecutor executor = new ShellLocalCommandExecutor()) {
            String command = OSUtil.isWindows() ? "cd" : "pwd";
            ShellLocalCommandExecutor.CommandResult result = executor.execute(command, directory.toString(), 10);

            assertEquals(0, result.exitCode());
            assertEquals(directory.toRealPath(), Path.of(result.stdout().trim()).toRealPath());
        } finally {
            try (var files = Files.walk(directory)) {
                files.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    @Test
    public void terminatesCommandAfterTimeout() {
        String command = OSUtil.isWindows()
                ? "ping -n 6 127.0.0.1 >nul"
                : "sleep 5";
        try (ShellLocalCommandExecutor executor = new ShellLocalCommandExecutor()) {
            ShellLocalCommandExecutor.CommandResult result = executor.execute(command, null, 1);

            assertTrue(result.timedOut());
        }
    }
}
