package cn.oyzh.easyshell.ssh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellConnState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * 验证不同系统与 shell 的 Exec 命令包装选择。
 */
public class ShellBaseSSHClientExecCommandTest {

    @Test
    public void linuxPosixShellUsesPosixPrefix() {
        TestClient client = new TestClient("Linux", "/bin/bash", "/bin", "/usr/bin");
        assertEquals("PATH='/bin:/usr/bin':\"$PATH\"; export PATH; echo ok", client.resolve("echo ok"));
    }

    @Test
    public void freeBsdCshUsesNativeSetenv() {
        TestClient client = new TestClient("FreeBSD", "/bin/csh", "/bin", "/usr/bin");
        assertEquals("setenv PATH '/bin:/usr/bin':\"$PATH\"; echo ok", client.resolve("echo ok"));
    }

    @Test
    public void windowsCmdUsesNativeSeparatorAndSetPrefix() {
        TestClient client = new TestClient("Windows", "C:\\Windows\\System32\\cmd.exe",
                "C:/Windows/System32", "C:/Program Files/Git/cmd");
        assertEquals("C:/Windows/System32;C:/Program Files/Git/cmd", client.getExportPath());
        assertEquals("set \"PATH=C:/Windows/System32;C:/Program Files/Git/cmd;%PATH%\" & echo ok",
                client.resolve("echo ok"));
    }

    @Test
    public void windowsPowershellUsesSingleQuotedPath() {
        TestClient client = new TestClient("Windows", "powershell.exe", "C:/Tools/O'Brien");
        assertEquals("try { $env:PATH='C:/Tools/O''Brien' + ';' + $env:PATH } catch {}; Write-Output ok",
                client.resolve("Write-Output ok"));
    }

    @Test
    public void windowsUnixShellUsesColonSeparatorAndPosixPrefix() {
        TestClient client = new TestClient("MINGW64_NT-10.0", "/usr/bin/bash",
                "C:/Windows/System32", "C:/Program Files/Git/bin");
        assertEquals("PATH='C:/Windows/System32:C:/Program Files/Git/bin':\"$PATH\"; export PATH; echo ok",
                client.resolve("echo ok"));
    }

    @Test
    public void windowsShellDetectionFallbackDoesNotUsePosixSyntax() {
        TestClient client = new TestClient("Windows", "bash", "C:/Windows/System32");
        assertEquals("echo ok", client.resolve("echo ok"));
    }

    @Test
    public void unknownShellKeepsCommandUntouched() {
        TestClient windows = new TestClient("Windows", "nu.exe", "C:/Tools");
        assertEquals("echo ok", windows.resolve("echo ok"));
        TestClient cmder = new TestClient("Windows", "cmder.exe", "C:/Tools");
        assertEquals("echo ok", cmder.resolve("echo ok"));
        TestClient unix = new TestClient("Linux", "/usr/bin/fish", "/usr/local/bin");
        assertEquals("echo ok", unix.resolve("echo ok"));
    }

    @Test
    public void missingOrUnsafePathKeepsCommandUntouched() {
        TestClient empty = new TestClient("Linux", "/bin/sh");
        empty.environment = List.of();
        assertEquals("echo ok", empty.resolve("echo ok"));

        TestClient unsafe = new TestClient("Linux", "/bin/sh", "/tmp/bad\npath");
        assertEquals("echo ok", unsafe.resolve("echo ok"));
    }

    @Test
    public void pathResolutionFailureKeepsCommandUntouched() {
        TestClient client = new TestClient("Linux", "/bin/sh", "/bin");
        client.failPathResolution = true;
        assertEquals("echo ok", client.resolve("echo ok"));
    }

    private static final class TestClient extends ShellBaseSSHClient {

        private final ObjectProperty<ShellConnState> state = new SimpleObjectProperty<>(ShellConnState.CLOSED);

        private boolean failPathResolution;

        private TestClient(String osType, String shellType, String... paths) {
            super(new ShellConnect());
            this.osType = osType;
            this.shellType = shellType;
            this.environment = List.of(paths);
        }

        private String resolve(String command) {
            return this.resolveExecCommand(command);
        }

        @Override
        protected String getExportPath(String separator) {
            if (this.failPathResolution) {
                throw new IllegalStateException("simulated path resolution failure");
            }
            return super.getExportPath(separator);
        }

        @Override
        public void start(int timeout) {
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public ObjectProperty<ShellConnState> stateProperty() {
            return this.state;
        }
    }
}
