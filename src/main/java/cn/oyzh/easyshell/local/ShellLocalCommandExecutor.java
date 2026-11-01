package cn.oyzh.easyshell.local;

import cn.oyzh.common.system.OSUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 本机命令执行器。
 *
 * <p>每次调用创建独立进程，可靠获取输出、退出码和命令结束状态。</p>
 *
 * @author oyzh
 * @since 2026-11-01
 */
public class ShellLocalCommandExecutor implements AutoCloseable {

    public static final long DEFAULT_TIMEOUT_SECONDS = 120;

    public static final long MAX_TIMEOUT_SECONDS = 600;

    public static final int MAX_OUTPUT_CHARS = 64 * 1024;

    private final Charset charset;

    private final Path defaultWorkingDirectory;

    private final Set<Process> processes = ConcurrentHashMap.newKeySet();

    private volatile boolean closed;

    public ShellLocalCommandExecutor() {
        this(Charset.defaultCharset());
    }

    public ShellLocalCommandExecutor(Charset charset) {
        this(Path.of(System.getProperty("user.dir")), charset);
    }

    public ShellLocalCommandExecutor(Path defaultWorkingDirectory, Charset charset) {
        this.defaultWorkingDirectory = defaultWorkingDirectory.toAbsolutePath().normalize();
        this.charset = charset == null ? Charset.defaultCharset() : charset;
    }

    public CommandResult execute(String command) {
        return this.execute(command, null, DEFAULT_TIMEOUT_SECONDS);
    }

    public CommandResult execute(String command, String workingDirectory, long timeoutSeconds) {
        if (this.closed) {
            return CommandResult.failure(command, this.defaultWorkingDirectory,
                    "Local command executor is closed.");
        }
        if (command == null || command.isBlank()) {
            return CommandResult.failure(command, this.defaultWorkingDirectory, "Command is required.");
        }
        Path resolvedWorkingDirectory;
        try {
            resolvedWorkingDirectory = this.resolveWorkingDirectory(workingDirectory);
        } catch (Exception ex) {
            return CommandResult.failure(command, this.defaultWorkingDirectory,
                    "Invalid working directory: " + ex.getMessage());
        }
        if (!Files.isDirectory(resolvedWorkingDirectory)) {
            return CommandResult.failure(command, resolvedWorkingDirectory,
                    "Working directory does not exist: " + resolvedWorkingDirectory);
        }
        Process process;
        try {
            ProcessBuilder builder = new ProcessBuilder(this.commandLine(command))
                    .directory(resolvedWorkingDirectory.toFile())
                    .redirectErrorStream(false);
            process = builder.start();
        } catch (IOException ex) {
            return CommandResult.failure(command, resolvedWorkingDirectory,
                    "Failed to start command: " + ex.getMessage());
        }
        this.processes.add(process);
        LimitedTextCollector stdout = new LimitedTextCollector();
        LimitedTextCollector stderr = new LimitedTextCollector();
        Thread stdoutReader = this.startReader(process.getInputStream(), stdout);
        Thread stderrReader = this.startReader(process.getErrorStream(), stderr);
        try {
            process.getOutputStream().close();
        } catch (IOException ignored) {
        }
        boolean timedOut = false;
        boolean interrupted = false;
        try {
            if (!process.waitFor(this.normalizeTimeout(timeoutSeconds), TimeUnit.SECONDS)) {
                timedOut = true;
                this.destroy(process);
                this.waitAfterDestroy(process);
            }
        } catch (InterruptedException ex) {
            interrupted = true;
            this.destroy(process);
            this.waitAfterDestroy(process);
            Thread.currentThread().interrupt();
        } finally {
            this.finishReaders(process, stdoutReader, stderrReader);
            this.processes.remove(process);
        }
        int exitCode = this.exitCode(process);
        return new CommandResult(command, resolvedWorkingDirectory, exitCode,
                stdout.text(), stderr.text(), timedOut, interrupted);
    }

    public Map<String, String> environment() {
        Map<String, String> environment = new LinkedHashMap<>();
        environment.put("os", System.getProperty("os.name"));
        environment.put("arch", System.getProperty("os.arch"));
        environment.put("user", System.getProperty("user.name"));
        environment.put("workingDirectory", this.defaultWorkingDirectory.toString());
        environment.put("shell", this.resolveShell());
        return environment;
    }

    public Path getDefaultWorkingDirectory() {
        return this.defaultWorkingDirectory;
    }

    private Path resolveWorkingDirectory(String workingDirectory) {
        if (workingDirectory == null || workingDirectory.isBlank()) {
            return this.defaultWorkingDirectory;
        }
        Path path = Path.of(workingDirectory);
        if (!path.isAbsolute()) {
            path = this.defaultWorkingDirectory.resolve(path);
        }
        return path.toAbsolutePath().normalize();
    }

    private long normalizeTimeout(long timeoutSeconds) {
        if (timeoutSeconds <= 0) {
            return DEFAULT_TIMEOUT_SECONDS;
        }
        return Math.min(timeoutSeconds, MAX_TIMEOUT_SECONDS);
    }

    private List<String> commandLine(String command) {
        List<String> commandLine = new ArrayList<>();
        if (OSUtil.isWindows()) {
            commandLine.add(this.resolveShell());
            commandLine.add("/d");
            commandLine.add("/c");
            commandLine.add(command);
        } else {
            commandLine.add(this.resolveShell());
            commandLine.add("-lc");
            commandLine.add(command);
        }
        return commandLine;
    }

    private String resolveShell() {
        if (OSUtil.isWindows()) {
            String comspec = System.getenv("ComSpec");
            return comspec == null || comspec.isBlank() ? "cmd.exe" : comspec;
        }
        String shell = System.getenv("SHELL");
        if (shell != null && !shell.isBlank()) {
            try {
                if (Files.isExecutable(Path.of(shell))) {
                    return shell;
                }
            } catch (Exception ignored) {
            }
        }
        return "/bin/sh";
    }

    private Thread startReader(InputStream inputStream, LimitedTextCollector collector) {
        return Thread.ofVirtual().name("local-tool-output").start(() -> {
            try (Reader reader = new InputStreamReader(inputStream, this.charset)) {
                char[] buffer = new char[4096];
                int length;
                while ((length = reader.read(buffer)) != -1) {
                    collector.append(buffer, length);
                }
            } catch (IOException ignored) {
            }
        });
    }

    private void finishReaders(Process process, Thread stdoutReader, Thread stderrReader) {
        this.joinReader(stdoutReader);
        this.joinReader(stderrReader);
        if (stdoutReader.isAlive() || stderrReader.isAlive()) {
            this.closeQuietly(process.getInputStream());
            this.closeQuietly(process.getErrorStream());
            this.joinReader(stdoutReader);
            this.joinReader(stderrReader);
        }
        if (stdoutReader.isAlive()) {
            stdoutReader.interrupt();
        }
        if (stderrReader.isAlive()) {
            stderrReader.interrupt();
        }
    }

    private void joinReader(Thread reader) {
        try {
            reader.join(2000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeQuietly(InputStream inputStream) {
        try {
            inputStream.close();
        } catch (IOException ignored) {
        }
    }

    private void waitAfterDestroy(Process process) {
        try {
            process.waitFor(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
        }
    }

    private int exitCode(Process process) {
        if (process.isAlive()) {
            return -1;
        }
        try {
            return process.exitValue();
        } catch (IllegalThreadStateException ex) {
            return -1;
        }
    }

    private void destroy(Process process) {
        process.descendants().forEach(handle -> {
            try {
                handle.destroyForcibly();
            } catch (Exception ignored) {
            }
        });
        process.destroyForcibly();
    }

    @Override
    public void close() {
        this.closed = true;
        for (Process process : this.processes) {
            this.destroy(process);
        }
        this.processes.clear();
    }

    public record CommandResult(String command, Path workingDirectory, int exitCode,
                                String stdout, String stderr, boolean timedOut, boolean interrupted) {

        public static CommandResult failure(String command, Path workingDirectory, String message) {
            return new CommandResult(command, workingDirectory, -1, "", message, false, false);
        }

        public String format() {
            StringBuilder result = new StringBuilder(256);
            result.append("exitCode: ").append(this.exitCode).append('\n');
            result.append("workingDirectory: ").append(this.workingDirectory).append('\n');
            if (this.timedOut) {
                result.append("timedOut: true\n");
            }
            if (this.interrupted) {
                result.append("interrupted: true\n");
            }
            appendSection(result, "STDOUT", this.stdout);
            appendSection(result, "STDERR", this.stderr);
            return result.toString();
        }

        private static void appendSection(StringBuilder result, String name, String content) {
            result.append(name).append(":\n");
            if (content == null || content.isEmpty()) {
                result.append("(empty)\n");
            } else {
                result.append(content);
                if (!content.endsWith("\n")) {
                    result.append('\n');
                }
            }
        }
    }

    private static final class LimitedTextCollector {

        private final StringBuilder content = new StringBuilder();

        private boolean truncated;

        synchronized void append(char[] buffer, int length) {
            int remaining = MAX_OUTPUT_CHARS - this.content.length();
            if (remaining > 0) {
                this.content.append(buffer, 0, Math.min(length, remaining));
            }
            if (length > remaining) {
                this.truncated = true;
            }
        }

        synchronized String text() {
            if (!this.truncated) {
                return this.content.toString();
            }
            return this.content + "\n... (output truncated)";
        }
    }
}
