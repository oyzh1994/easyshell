package cn.oyzh.easyshell.x11;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.ProcessUtil;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.plus.information.MessageBox;

import java.io.File;

/**
 * x11管理器
 *
 * @author oyzh
 * @since 2025/03/08
 */

public class X11Manager {

    /**
     * x11进程
     */
    private static Process x11Process;

    /**
     * 当前存储对象
     */
    private static final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 启动x11服务
     */
    public synchronized static void startXServer() {
        // x11进程存在
        if (x11Process != null && x11Process.isAlive()) {
            return;
        }
        // 检查工作目录
        if (setting.x11WorkDir() == null) {
            JulLog.warn("x11 WorkDir is null");
            return;
        }
        if (OSUtil.isWindows()) {
            startXServer_windows();
        } else if (OSUtil.isMacOS()) {
            startXServer_macos();
        }
    }

    /**
     * windows下启动x-server
     */
    private static void startXServer_windows() {
        // 判断进程是否存在
        String[] processName = setting.x11Binary();
        if (ProcessUtil.isProcessRunning(processName)) {
            return;
        }
        // 异步启动
        ThreadUtil.start(() -> {
            try {
                // 寻找存在的二进制命令
                String bin = X11Util.findExist(setting.x11WorkDir(), setting.x11Binary());
                // 命令
                StringBuilder command = new StringBuilder("cmd.exe /c start ");
                // 二进制程序名称
                command.append(bin);
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
                File dir = new File(setting.x11WorkDir());
                // 启动进程
                x11Process = RuntimeUtil.exec(command.toString(), null, dir);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

//    /**
//     * windows下启动x-server
//     */
//    private static void startXServer_windows() {
//        // 判断进程是否存在
//        String[] processName = {"vcxsrv.exe", "XWin_MobaX.exe"};
//        if (ProcessUtil.isProcessRunning(processName)) {
//            return;
//        }
//        // 异步启动
//        ThreadUtil.start(() -> {
//            StringBuilder command = new StringBuilder("cmd.exe /c start ");
//            // moba
//            if (StringUtil.equals(x11_type, "moba")) {
//                command.append("XWin_MobaX.exe");
//            } else {// vcxsrv
//                command.append("vcxsrv.exe");
//            }
//            // vcxsrv
//            // 剪切板互通
//            command.append(" -clipboard ");
//            // 鉴权
//            command.append(" -ac");
//            // gpu加速
//            command.append(" -wgl");
//            // 多窗口
//            command.append(" -multiwindow");
//            // 静默处理错误
//            command.append(" -silent-dup-error");
//            // 关闭托盘
//            command.append(" -notrayicon");
////            // 指定分辨率
////            command.append(" -screen 1920x1080");
//            // 工作目录
//            String dir;
//            if (StringUtil.equals(x11_type, "moba")) {
//                dir = ResourceUtil.getResource("/bin/moba/").getFile();
//            } else {// vcxsrv
//                dir = ResourceUtil.getResource("/bin/vcxsrv/").getFile();
//            }
//            if (dir.startsWith("/")) {
//                dir = dir.substring(1);
//            }
//            // 启动进程
//            x11Process = RuntimeUtil.exec(command.toString(), null, new File(dir));
//        });
//    }

    /**
     * macos下启动x-server
     */
    private static void startXServer_macos() {
        // 判断进程是否存在
        String[] processName = {"XQuartz"};
        if (ProcessUtil.isProcessRunning(processName)) {
            return;
        }
        // 异步启动
        ThreadUtil.start(() -> {
            try {
                // 寻找存在的二进制命令
                String bin = X11Util.findExist(setting.x11WorkDir(), setting.x11Binary());
                // 工作目录
                File dir = new File(setting.x11WorkDir());
                // 构建进程
                ProcessBuilder processBuilder = new ProcessBuilder(bin);
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
