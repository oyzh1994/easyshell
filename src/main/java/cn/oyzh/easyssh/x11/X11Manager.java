package cn.oyzh.easyssh.x11;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.ProcessUtil;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyssh.domain.SSHSetting;
import cn.oyzh.easyssh.store.SSHSettingStore;
import cn.oyzh.fx.plus.information.MessageBox;
import lombok.experimental.UtilityClass;

import java.io.File;

/**
 * 程序主入口
 *
 * @author oyzh
 * @since 2025/03/08
 */
@UtilityClass
public class X11Manager {

    /**
     * x11类型
     * moba 默认
     * vcxsrv
     */
    public static String x11_type = "moba";

    /**
     * x11进程
     */
    private static Process x11Process;

    /**
     * 当前存储对象
     */
    private static final SSHSetting setting = SSHSettingStore.SETTING;

    /**
     * 启动x11服务
     */
    public synchronized static void startXServer() {
        if (x11Process != null && x11Process.isAlive()) {
            return;
        }
        if (OSUtil.isWindows()) {
            startXServer_windows();
        } else if (OSUtil.isMacOS()) {
//            startXServer_macos_arm64();
            startXServer_macos();
        }
    }

    private static void startXServer_windows() {
        // 判断进程是否存在
        String[] processName = {"vcxsrv.exe", "XWin_MobaX.exe"};
        if (ProcessUtil.isProcessRunning(processName)) {
            return;
        }
        // 异步启动
        ThreadUtil.start(() -> {
            StringBuilder command = new StringBuilder("cmd.exe /c start ");
            // moba
            if (StringUtil.equals(x11_type, "moba")) {
                command.append("XWin_MobaX.exe");
            } else {// vcxsrv
                command.append("vcxsrv.exe");
            }
            // vcxsrv
            // 剪切板互通
            command.append(" -clipboard ");
            // 鉴权
            command.append(" -ac");
            // gpu加速
            command.append(" -wgl");
            // 多窗口
            command.append(" -multiwindow");
            // 静默处理错误
            command.append(" -silent-dup-error");
            // 关闭托盘
            command.append(" -notrayicon");
//            // 指定分辨率
//            command.append(" -screen 1920x1080");
            // 工作目录
            String dir;
            if (StringUtil.equals(x11_type, "moba")) {
                dir = ResourceUtil.getResource("/bin/moba/").getFile();
            } else {// vcxsrv
                dir = ResourceUtil.getResource("/bin/vcxsrv/").getFile();
            }
            if (dir.startsWith("/")) {
                dir = dir.substring(1);
            }
            // 启动进程
            x11Process = RuntimeUtil.exec(command.toString(), null, new File(dir));
        });
    }

    private static void startXServer_macos() {
        // 判断进程是否存在
        String[] processName = {"XQuartz"};
        if (ProcessUtil.isProcessRunning(processName)) {
            return;
        }
        // 检查二进制文件
        if (setting.x11WorkDir() == null) {
            JulLog.warn("x11 WorkDir is null");
            return;
        }
        // 异步启动
        ThreadUtil.start(() -> {
            try {
                // 工作目录
                File dir = new File(setting.x11WorkDir());
                // 构建进程
                ProcessBuilder processBuilder = new ProcessBuilder(setting.x11Binary());
                processBuilder.directory(dir);
                // 启动进程
                x11Process = processBuilder.start();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

//    private static void startXServer_macos_arm64() {
//        // 判断进程是否存在
//        String[] processName = {"XQuartz"};
//        if (ProcessUtil.isProcessRunning(processName)) {
//            return;
//        }
//        // 异步启动
//        ThreadUtil.start(() -> {
//            try {
//                StringBuilder command = new StringBuilder("startx");
//                // 工作目录
//                String dir = ResourceUtil.getResource("/bin/xquartz").getFile();
//                ProcessBuilder processBuilder = new ProcessBuilder(command.toString());
//                processBuilder.directory(new File(dir));
//                // 启动进程
//                x11Process = processBuilder.start();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                MessageBox.exception(ex);
//            }
//        });
//    }

    /**
     * 停止x11服务
     */
    public synchronized static void stopXServer() {
        if (x11Process != null) {
            try {
                x11Process.destroy();
                x11Process = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    static {
        // 注册钩子
        RuntimeUtil.addShutdownHook(new Thread(X11Manager::stopXServer));
    }
}
