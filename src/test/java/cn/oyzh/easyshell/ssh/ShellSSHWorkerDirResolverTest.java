package cn.oyzh.easyshell.ssh;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static cn.oyzh.easyshell.ssh.ShellOscShellIntegration.Platform.FREEBSD;
import static cn.oyzh.easyshell.ssh.ShellOscShellIntegration.Platform.OPENBSD;
import static cn.oyzh.easyshell.ssh.ShellOscShellIntegration.Platform.UNIX;
import static cn.oyzh.easyshell.ssh.ShellOscShellIntegration.Platform.WINDOWS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 终端工作目录解析测试
 */
public class ShellSSHWorkerDirResolverTest {

    private static final String ESC = "\u001B";

    private static final String BEL = "\u0007";

    private static final String ST = ESC + "\\";

    @Test
    public void parsesOsc7WithBelTerminator() {
        String output = ESC + "]7;file://server/srv/app" + BEL;
        assertEquals("/srv/app", ShellSSHUtil.resolveWorkerDir(output, "/home/demo"));
    }

    @Test
    public void oscParserCanBeUsedIndependently() {
        ShellOscParser parser = new ShellOscParser();
        assertEquals("/srv/app", parser.parseCwd(ESC + "]7;file://server/srv/app" + BEL));
        assertEquals("user@host:/srv/app$",
                ShellOscParser.removeOsc("user@host:/srv/app$" + ESC + "]0;title" + BEL));
    }

    @Test
    public void recognizesSupportedShellsForOscIntegration() {
        assertTrue(ShellOscShellIntegration.isSupported("/bin/bash", UNIX));
        assertTrue(ShellOscShellIntegration.isSupported("-zsh", UNIX));
        assertTrue(ShellOscShellIntegration.isSupported("/bin/csh", UNIX));
        assertTrue(ShellOscShellIntegration.isSupported("/bin/tcsh", FREEBSD));
        assertTrue(ShellOscShellIntegration.isSupported("/bin/sh", FREEBSD));
        assertTrue(ShellOscShellIntegration.isSupported("/bin/ksh", OPENBSD));
        assertTrue(ShellOscShellIntegration.isSupported("/bin/sh", OPENBSD));
        assertFalse(ShellOscShellIntegration.isSupported("/bin/ksh", UNIX));
        assertFalse(ShellOscShellIntegration.isSupported("/bin/sh", UNIX));
        assertNull(ShellOscShellIntegration.command("/bin/sh", UNIX));

        assertFalse(ShellOscShellIntegration.isSupported("cmd.exe", UNIX));
        assertTrue(ShellOscShellIntegration.isSupported("cmd.exe", WINDOWS));
        assertTrue(ShellOscShellIntegration.isSupported("C:\\Windows\\System32\\cmd.exe", WINDOWS));
        assertTrue(ShellOscShellIntegration.isSupported("powershell.exe", WINDOWS));
        assertTrue(ShellOscShellIntegration.isSupported(
                "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe", WINDOWS));
        assertTrue(ShellOscShellIntegration.isSupported("pwsh", WINDOWS));
        assertTrue(ShellOscShellIntegration.isWindowsShell("C:\\Windows\\System32\\cmd.exe"));
        assertTrue(ShellOscShellIntegration.isWindowsShell("pwsh.exe"));
        assertFalse(ShellOscShellIntegration.isWindowsShell("/usr/bin/bash"));
    }

    @Test
    public void shellIntegrationEmitsOsc7AndRestoresEcho() {
        ShellOscShellIntegration.Command bash = ShellOscShellIntegration.command("/bin/bash", UNIX);
        assertTrue(bash.disableEcho().contains("stty -echo"));
        assertTrue(bash.disableEcho().contains("sleep 1"));
        assertTrue(bash.disableEcho().contains("set +o history"));
        assertTrue(bash.disableEcho().contains("__easyshell_osc7_hist__"));
        assertTrue(bash.prepare().contains("PS1="));
        assertTrue(bash.hook().contains("]7;file://%s%s"));
        assertTrue(bash.hook().contains("PROMPT_COMMAND"));
        assertTrue(bash.hook().contains("declare -a"));
        assertTrue(bash.hook().contains("PROMPT_COMMAND=(__easyshell_osc7__"));
        assertTrue(bash.restoreEcho().contains("stty echo"));
        assertTrue(bash.restoreEcho().contains("history -d"));
        assertTrue(bash.restoreEcho().contains("set -o history"));
        assertTrue(bash.restoreEcho().contains("BASH_VERSINFO[0]"));

        ShellOscShellIntegration.Command openBsdKsh = ShellOscShellIntegration.command("/bin/ksh", OPENBSD);
        assertTrue(openBsdKsh.disableEcho().contains("stty -echo"));
        assertTrue(openBsdKsh.prepare().contains("PS1="));
        assertTrue(openBsdKsh.hook().contains("]9;9;\\w\\a"));
        assertTrue(openBsdKsh.hook().contains("\\[\\e"));
        assertTrue(openBsdKsh.restoreEcho().contains("stty echo"));

        ShellOscShellIntegration.Command zsh = ShellOscShellIntegration.command("/bin/zsh", UNIX);
        assertTrue(zsh.disableEcho().contains("unsetopt zle"));
        assertTrue(zsh.disableEcho().contains("__easyshell_zle_on__"));
        assertTrue(zsh.disableEcho().contains("fc -p"));
        assertTrue(zsh.disableEcho().contains("histignorespace"));
        assertTrue(zsh.prepare().contains("\\033[2K"));
        assertTrue(zsh.hook().contains("add-zsh-hook precmd"));
        assertTrue(zsh.hook().contains("precmd_functions+=(__easyshell_osc7__)"));
        assertTrue(zsh.restoreEcho().contains("[[ \"$__easyshell_zle_on__\" == 1 ]] && setopt zle"));
        assertTrue(zsh.restoreEcho().contains("fc -P"));
        assertTrue(zsh.restoreEcho().contains("HISTORY_IGNORE"));

        assertFalse(bash.disableEcho().contains("history -c"));
        assertFalse(zsh.disableEcho().contains("HISTSIZE=0"));

        ShellOscShellIntegration.Command cmd = ShellOscShellIntegration.command("cmd.exe", WINDOWS);
        assertEquals("prompt $E]9;9;$P$E\\$P$G", cmd.hook());
        assertNull(cmd.disableEcho());

        ShellOscShellIntegration.Command powershell =
                ShellOscShellIntegration.command("powershell.exe", WINDOWS);
        assertTrue(powershell.hook().contains("function global:prompt"));
        assertTrue(powershell.hook().contains("Get-Location"));
        assertTrue(powershell.hook().contains("[char]27"));
        assertTrue(powershell.hook().contains("__easyshell_osc7_installed__"));
        assertNull(powershell.disableEcho());
    }

    @Test
    public void freeBsdShellsReportDirectoryWithoutPromptHookFiles() {
        ShellOscShellIntegration.Command sh = ShellOscShellIntegration.command("/bin/sh", FREEBSD);
        assertTrue(sh.hook().contains("\\e]9;9;\\w\\a"));
        assertFalse(sh.hook().contains("file://"));
        assertFalse(sh.hook().contains("\\["));
        assertTrue(sh.restoreEcho().contains("PS1=$__easyshell_ps1__"));
        assertFalse(sh.hook().contains(".profile"));

        ShellOscShellIntegration.Command csh = ShellOscShellIntegration.command("/bin/csh", FREEBSD);
        assertTrue(csh.hook().contains("alias cwdcmd"));
        assertTrue(csh.hook().contains("__easyshell_previous_cwdcmd__"));
        assertTrue(csh.hook().contains("]9;9;%s"));
        assertFalse(csh.hook().contains("file://"));
        assertTrue(csh.restoreEcho().contains("set prompt="));
    }

    @Test
    public void windowsHookIsWrittenAsSingleLineWithoutUnixEchoControl() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            assertTrue(ShellOscShellIntegration.install(output, "cmd.exe", WINDOWS));
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
        String command = output.toString(StandardCharsets.US_ASCII);
        assertTrue(command.contains("prompt $E]9;9;$P$E\\$P$G"));
        assertTrue(command.endsWith("\r"));
        assertFalse(command.contains("stty"));
    }

    @Test
    public void powershellHookIsReadWithoutPsReadLineEcho() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            assertTrue(ShellOscShellIntegration.install(output, "powershell.exe", WINDOWS));
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
        String command = output.toString(StandardCharsets.US_ASCII);
        assertTrue(command.contains("[Console]::ReadKey($true)"));
        assertTrue(command.contains("function global:prompt"));
        assertTrue(command.indexOf("[Console]::ReadKey($true)")
                < command.indexOf("function global:prompt"));
    }

    @Test
    public void parsesOsc7WithStringTerminator() {
        String output = ESC + "]7;file://localhost/tmp/project" + ST;
        assertEquals("/tmp/project", ShellSSHUtil.resolveWorkerDir(output, "/home/demo"));
    }

    @Test
    public void decodesUtf8PercentEncodingWithoutTreatingPlusAsSpace() {
        String output = ESC + "]7;file:///tmp/a%20b/%E4%B8%AD%E6%96%87+c" + BEL;
        assertEquals("/tmp/a b/中文+c", ShellSSHUtil.resolveWorkerDir(output, null));
    }

    @Test
    public void parsesOsc7AcrossChunks() {
        ShellWorkerDirResolver resolver = new ShellWorkerDirResolver();
        assertNull(resolver.resolve(ESC + "]7;file://host/opt/my", "/home/demo"));
        assertEquals("/opt/my project", resolver.resolve("%20project" + BEL, "/home/demo"));
    }

    @Test
    public void parsesOsc7WhenEveryCharacterArrivesSeparately() {
        ShellWorkerDirResolver resolver = new ShellWorkerDirResolver();
        String output = ESC + "]7;file://host/srv/%E4%B8%AD%E6%96%87" + ST;
        String dir = null;
        for (int i = 0; i < output.length(); i++) {
            String resolved = resolver.resolve(output.substring(i, i + 1), "/home/demo");
            if (resolved != null) {
                dir = resolved;
            }
        }
        assertEquals("/srv/中文", dir);
    }

    @Test
    public void usesLatestOscDirectory() {
        String output = ESC + "]7;file://host/old" + BEL
                + "some output\n"
                + ESC + "]7;file://host/new" + ST;
        assertEquals("/new", ShellSSHUtil.resolveWorkerDir(output, null));
    }

    @Test
    public void parsesWindowsTerminalOsc99() {
        String output = ESC + "]9;9;C:\\Users\\demo\\project" + BEL;
        assertEquals("/C:/Users/demo/project", ShellSSHUtil.resolveWorkerDir(output, null));
    }

    @Test
    public void parsesWindowsOsc7FileUri() {
        String output = ESC + "]7;file://server/C:/Users/demo/project" + ST;
        assertEquals("/C:/Users/demo/project", ShellSSHUtil.resolveWorkerDir(output, null));
    }

    @Test
    public void osc99KeepsRawSpacesAndPercentSigns() {
        String output = ESC + "]9;9;/srv/100% real path" + ST;
        assertEquals("/srv/100% real path", ShellSSHUtil.resolveWorkerDir(output, null));
    }

    @Test
    public void fallsBackToPromptWhenOscIsUnsupported() {
        String output = ESC + "]0;terminal title" + BEL + "user@host:/var/log$";
        assertEquals("/var/log", ShellSSHUtil.resolveWorkerDir(output, "/home/demo"));
    }

    @Test
    public void fallsBackToPromptWhenOscUriIsInvalid() {
        String output = ESC + "]7;https://example.com/tmp" + BEL + "user@host:/srv/app$";
        assertEquals("/srv/app", ShellSSHUtil.resolveWorkerDir(output, "/home/demo"));
    }

    @Test
    public void keepsLiteralPercentWhenEncodingIsInvalid() {
        String output = ESC + "]7;file:///tmp/%ZZ" + BEL + "user@host:/data$";
        assertEquals("/tmp/%ZZ", ShellSSHUtil.resolveWorkerDir(output, "/home/demo"));
    }

    @Test
    public void promptFallbackStillExpandsHome() {
        assertEquals("/home/demo", ShellSSHUtil.resolveWorkerDir("[user@host ~]$", "/home/demo"));
    }

    @Test
    public void promptFallbackStillScansPreviousLines() {
        String output = "user@host:/srv/app$\ncommand output without a prompt";
        assertEquals("/srv/app", ShellSSHUtil.resolveWorkerDir(output, "/home/demo"));
    }

    @Test
    public void preservesPromptStateUntilLineIsComplete() {
        ShellWorkerDirResolver resolver = new ShellWorkerDirResolver();
        assertNull(resolver.resolve("user@host:", "/home/demo"));
        assertEquals("/opt/project", resolver.resolve("/opt/project$", "/home/demo"));
    }

    @Test
    public void resetsIncrementalState() {
        ShellWorkerDirResolver resolver = new ShellWorkerDirResolver();
        assertNull(resolver.resolve(ESC + "]7;file://host/tmp", "/home/demo"));
        resolver.reset();
        assertNull(resolver.resolve("still-not-terminated" + BEL, "/home/demo"));
    }
}
