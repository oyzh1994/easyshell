package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.tty.TtyProcessTtyConnector;
import cn.oyzh.fx.tty.TtyTermWidget;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import kotlin.text.Charsets;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * shell进程终端组件
 *
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellProcessTermWidget extends TtyTermWidget {

    /**
     * 设置
     */
    protected final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 构造方法
     */
    public ShellProcessTermWidget() {
        super(new ShellSettingsProvider());
        ShellTerminalUtil.applySetting(this, this.setting);
    }

    /**
     * 获取进程命令
     *
     * @return 进程命令
     */
    protected String[] getProcessCommand() {
        // 如果设置了指定类型的终端，则直接返回
        String termType = this.setting.getTermType();
        if (StringUtil.isNotBlank(termType)) {
            if (OSUtil.isMacOS()) {
                return new String[]{termType, "--login"};
            }
            if (OSUtil.isWindows()) {
                if ("git-sh".equals(termType)) {
                    if (FileUtil.exists("C:\\Program Files (x86)\\Git\\bin\\sh.exe")) {
                        return new String[]{"C:\\Program Files (x86)\\Git\\bin\\sh.exe", "--login", "-i"};
                    }
                    return new String[]{"C:\\Program Files\\Git\\bin\\sh.exe", "--login", "-i"};
                }
                if ("git-bash".equals(termType)) {
                    String filePath = "C:\\Program Files (x86)\\Git\\bin\\bash.exe";
                    if (FileUtil.exists(filePath)) {
                        return new String[]{filePath, "--login", "-i"};
                    }
                    return new String[]{"C:\\Program Files\\Git\\bin\\bash.exe", "--login", "-i"};
                }
                if ("msys2-bash".equals(termType)) {
                    return new String[]{"C:\\msys64\\usr\\bin\\bash.exe", "--login", "-i"};
                }
                if ("cygwin-bash".equals(termType)) {
                    String filePath = "C:\\cygwin64\\bin\\bash.exe";
                    if (FileUtil.exists(filePath)) {
                        return new String[]{filePath, "--login", "-i"};
                    }
                    return new String[]{"C:\\cygwin\\bin\\bash.exe", "--login", "-i"};
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

    /**
     * 创建进程
     *
     * @return 进程对象
     * @throws IOException 异常
     */
    protected PtyProcess createProcess() throws IOException {
        Map<String, String> envs = this.getEnvironments();
        String[] command = this.getProcessCommand();
        boolean cygwin = command[0].contains("sh.exe");
        // 不支持windows arm
        if (OSUtil.isWindows() && OSUtil.isAarch64()) {
            cygwin = false;
        }
        boolean useWinConPty = !cygwin;
        String workingDirectory = Path.of(".").toAbsolutePath().normalize().toString();
        JulLog.info("Starting {} in {}", String.join(" ", command), workingDirectory);
        return new PtyProcessBuilder()
                .setDirectory(workingDirectory)
                .setInitialColumns(80)
                .setInitialRows(24)
                .setCommand(command)
                .setEnvironment(envs)
                .setConsole(false)
                .setCygwin(cygwin)
                .setUseWinConPty(useWinConPty)
                // 这个会导致输出混乱，不要为true
                .setRedirectErrorStream(false)
                .setWindowsAnsiColorEnabled(true)
                .start();
    }

    @Override
    public TtyProcessTtyConnector createTtyConnector() throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new TtyProcessTtyConnector(process, Charset.defaultCharset(), List.of(command)) {
            @Override
            public String getName() {
                return "default-tty";
            }
        };
    }

    //    public TtyProcessTtyConnector createTtyConnector(Charset charset) throws IOException {
    //        PtyProcess process = this.createProcess();
    //        String[] command = this.getProcessCommand();
    //        return new TtyProcessTtyConnector(process, charset, Arrays.asList(command));
    //    }

    /**
     * 环境列表
     */
    private HashMap<String, String> envs;

    /**
     * 获取环境
     *
     * @return 结果
     */
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

    /**
     * 添加环境变量
     *
     * @param key   名称
     * @param value 值
     */
    public void putEnvironment(String key, String value) {
        this.getEnvironments().put(key, value);
    }

    //    /**
    //     * 创建zModem协议的tty连接器
    //     *
    //     * @param connector tty连接器
    //     * @return ShellZModemTtyConnector
    //     */
    //    public TtyZModemTtyConnector createZModemTtyConnector(TtyProcessTtyConnector connector) {
    //        return new TtyZModemTtyConnector(this.getTerminal(), connector);
    //    }
}
