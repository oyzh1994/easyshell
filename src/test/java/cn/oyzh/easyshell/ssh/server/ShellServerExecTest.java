package cn.oyzh.easyshell.ssh.server;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * 远端 shell 类型探测测试
 */
public class ShellServerExecTest {

    @Test
    public void extractsShellPathFromCshExecNoise() {
        assertEquals("/bin/csh", ShellServerExec.parseUnixShellType(
                "export: Command not found.\n/bin/csh\n"));
    }

    @Test
    public void extractsLastShellPathFromMultipleLines() {
        assertEquals("/bin/zsh", ShellServerExec.parseUnixShellType(
                "warning: something\n/bin/bash\n/bin/zsh\n"));
    }

    @Test
    public void ignoresNonPathOutput() {
        assertNull(ShellServerExec.parseUnixShellType("export: Command not found.\n"));
        assertNull(ShellServerExec.parseUnixShellType("   \n"));
        assertNull(ShellServerExec.parseUnixShellType(null));
    }
}
