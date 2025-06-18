package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.zmodem.ZModemPtyConnectorAdaptor1;
import cn.oyzh.jeditermfx.app.pty.TtyConnectorWaitFor;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXHyperlinkFilter;
import com.jediterm.terminal.ui.FXJediTermWidget;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import kotlin.text.Charsets;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellDefaultTermWidget extends FXJediTermWidget {

    /**
     * 设置
     */
    protected final ShellSetting setting = ShellSettingStore.SETTING;

    public ShellDefaultTermWidget() {
        this(new ShellSettingsProvider());
    }

    public ShellDefaultTermWidget(SettingsProvider provider) {
        super(provider);
        if (this.setting.isTermParseHyperlink()) {
            this.addHyperlinkFilter(new FXHyperlinkFilter());
        }
    }

    protected String[] getProcessCommand() {
        // 如果设置了指定类型的终端，则直接返回
        String termType = this.setting.getTermType();
        if (StringUtil.isNotBlank(termType)) {
            if (OSUtil.isMacOS()) {
                return new String[]{termType, "--login"};
            }
            if (OSUtil.isWindows()) {
                if ("git-sh".equals(termType)) {
                    return new String[]{"C:\\Program Files\\Git\\bin\\sh.exe", "--login", "-i"};
                }
                if ("git-bash".equals(termType)) {
                    return new String[]{"C:\\Program Files\\Git\\bin\\bash.exe", "--login", "-i"};
                }
                if ("cmd.exe".equals(termType)) {
                    return new String[]{termType, "-l"};
                }
                return new String[]{termType};
            }
            return new String[]{termType};
        }
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

    public TtyConnector createTtyConnector() throws IOException {
        return this.createTtyConnector(StandardCharsets.UTF_8);
    }

    public TtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new ShellDefaultTtyConnector(process, charset, Arrays.asList(command));
    }

    /**
     * 环境列表
     */
    private HashMap<String, String> envs;

    public Map<String, String> getEnvironments() {
        if (this.envs == null) {
            this.envs = new HashMap<>(System.getenv());
            if (OSUtil.isMacOS()) {
                this.envs.put("LC_CTYPE", Charsets.UTF_8.name());
                this.envs.put("LANG", "en_US.utf-8");
                this.envs.put("TERM", "xterm-256color");
            } else if (OSUtil.isLinux()) {
                this.envs.put("LANG", "en_US.utf-8");
                this.envs.put("TERM", "xterm-256color");
            } else if (OSUtil.isWindows()) {
                this.envs.put("TERM", "xterm-256color");
            }
        }
        return this.envs;
    }

    // protected void fixBashEnvironment(Map<String, String> envs, String bash) {
    //     if (OSUtil.isWindows()) {
    //         String comSpec = envs.get("ComSpec");
    //         if (StringUtil.isBlank(comSpec)) {
    //             if ("cmd.exe".equals(bash)) {
    //                 comSpec = RuntimeUtil.execForStr("where cmd.exe");
    //             } else if ("powershell.exe".equals(bash)) {
    //                 comSpec = RuntimeUtil.execForStr("where powershell.exe");
    //             } else {
    //                 comSpec = bash;
    //             }
    //         } else if ("cmd.exe".equals(bash) && !StringUtil.containsIgnoreCase(comSpec, "cmd.exe")) {
    //             comSpec = RuntimeUtil.execForStr("where cmd.exe");
    //         } else if ("powershell.exe".equals(bash) && !StringUtil.containsIgnoreCase(comSpec, "powershell.exe")) {
    //             comSpec = RuntimeUtil.execForStr("where powershell.exe");
    //         } else if (bash.contains("bash.exe") && !StringUtil.containsIgnoreCase(comSpec, "bash.exe")) {
    //             comSpec = bash;
    //         } else if (bash.contains("sh.exe") && !StringUtil.containsIgnoreCase(comSpec, "sh.exe")) {
    //             comSpec = bash;
    //         }
    //         envs.put("ComSpec", comSpec);
    //     }
    // }

    /**
     * 添加环境变量
     *
     * @param key   名称
     * @param value 值
     */
    public void putEnvironment(String key, String value) {
        this.getEnvironments().put(key, value);
    }

    public void openSession() throws IOException {
        if (this.canOpenSession()) {
            this.openSession(this.createTtyConnector());
        }
    }

    public void openSession(TtyConnector ttyConnector) {
        if (this.canOpenSession()) {
            FXJediTermWidget session = this.createTerminalSession(ttyConnector);
//            if (ttyConnector instanceof ShellDefaultTtyConnector loggingConnector) {
//                loggingConnector.setWidget(session);
//            }
            session.start();
        }
    }

    public TtyConnectorWaitFor onTermination(IntConsumer terminationCallback) {
        return new TtyConnectorWaitFor(this.getTtyConnector(),
                this.getExecutorServiceManager().getUnboundedExecutorService(),
                terminationCallback);
    }

    @Override
    public ShellDefaultTtyConnector getTtyConnector() {
        if (super.getTtyConnector() instanceof ZModemPtyConnectorAdaptor1 adaptor) {
            return (ShellDefaultTtyConnector) adaptor.getConnector();
        }
        return (ShellDefaultTtyConnector) super.getTtyConnector();
    }

//    public double getWidth() {
//        return NodeUtil.getWidth(this.getComponent());
//    }
//
//    public double getHeight() {
//        return NodeUtil.getHeight(this.getComponent());
//    }

    public TermSize getTermSize() {
        return this.getTtyConnector().getTermSize();
    }

    @Override
    public void close() {
        try {
            super.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        SystemUtil.gcLater();
    }
}
