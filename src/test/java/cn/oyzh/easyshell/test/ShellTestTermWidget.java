package cn.oyzh.easyshell.test;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ssh2.ShellSSHTtyConnector;
import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import cn.oyzh.fx.tty.TtyTermWidget;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.jediterm.terminal.ui.settings.FXDefaultSettingsProvider;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import kotlin.text.Charsets;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ShellTestTermWidget extends TtyTermWidget {

    public ShellTestTermWidget( ) {
        super(new FXDefaultSettingsProvider());
    }

    private Map<String, String> envs;

    public Map<String, String> getEnvironments() {
        if (this.envs == null) {
            this.envs = new HashMap<>(System.getenv());
            if (OSUtil.isMacOS()) {
                this.envs.put("LC_CTYPE", Charsets.UTF_8.name());
                this.envs.put("LANG", "en_US.utf-8");
                // this.envs.put("TERM", "xterm-256color");
            } else if (OSUtil.isLinux()) {
                this.envs.put("LANG", "en_US.utf-8");
                // this.envs.put("TERM", "xterm-256color");
            } else if (OSUtil.isWindows()) {
                // this.envs.put("TERM", "xterm-256color");
            }
            this.envs.put("TERM", "xterm");
        }
        return this.envs;
    }

    protected String[] getProcessCommand() {
        String[] command = new String[]{"/bin/bash"};
        Map<String, String> envs = this.getEnvironments();
        if (OSUtil.isWindows()) {
            command = new String[]{"cmd.exe"};
            //            command = new String[]{"powershell.exe"};
        } else if (OSUtil.isLinux()) {
            String shell = envs.get("SHELL");
            if (shell == null) {
                shell = "/bin/bash";
            }
            command = new String[]{shell, "-l"};
        } else if (OSUtil.isMacOS()) {
            String shell = envs.get("SHELL");
            if (shell == null) {
                shell = "/bin/bash";
            }
            command = new String[]{shell, "--login"};
        }
        return command;
    }


    protected PtyProcess createProcess() throws IOException {
        Map<String, String> envs = this.getEnvironments();
        String[] command = this.getProcessCommand();
        // this.fixBashEnvironment(envs, command[0]);
        String workingDirectory = Path.of(".").toAbsolutePath().normalize().toString();
        JulLog.info("Starting {} in {}", String.join(" ", command), workingDirectory);
        return new PtyProcessBuilder()
                .setDirectory(workingDirectory)
                .setInitialColumns(80)
                .setInitialRows(24)
                .setCommand(command)
                .setEnvironment(envs)
                .setConsole(false)
                .setUseWinConPty(false)
                // 这个会导致输出混乱，不要为true
                .setRedirectErrorStream(false)
                .setWindowsAnsiColorEnabled(true)
                .start();
    }

    @Override
    public TtyConnector createTtyConnector() throws IOException {
        return this.createTtyConnector(Charset.defaultCharset());
    }

    public ShellTestTtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        ShellTestTtyConnector connector = new ShellTestTtyConnector(process, charset, Arrays.asList(command));
        return connector;
    }

    @Override
    public ShellSSHTtyConnector getTtyConnector() {
        return (ShellSSHTtyConnector) super.getTtyConnector();
    }


}
