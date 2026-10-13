package cn.oyzh.easyshell.x11;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.ProcessUtil;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import io.github.oyzh1994.xserver.ServerOptions;
import io.github.oyzh1994.xserver.XServer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * x11管理器
 *
 * @author oyzh
 * @since 2025-03-08
 */
public class ShellX11Manager {

    /**
     * x11进程
     */
    @Deprecated
    private static Process x11Process;

    /**
     * 当前存储对象
     */
    private static final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * x服务
     */
    private static XServer xserver;

    /**
     * 启动x11服务，内建
     */
    public synchronized static void startXServer_built_in() throws Exception {
        if (xserver != null && xserver.isRunning()) {
            //throw new RuntimeException("x server is running!");
            return;
        }
        List<Object> list = new ArrayList<>();
        list.add("--display");
        list.add((setting.getX11Port() - 6000) + "");
        if (setting.isX11Glx()) {
            list.add("--glx");
        } else {
            list.add("--no-glx");
        }
        if (setting.isX11Local()) {
            list.add("--local");
        }
        if (setting.isX11FocusOnClick()) {
            list.add("--focus-on-click");
        }
        if (setting.isX11SingleWindow()) {
            list.add("--single-window");
        }
        String[] args = list.toArray(new String[]{});
        ServerOptions options = ServerOptions.parse(args);
        options.fitToDisplay();
        xserver = new XServer(options);
        xserver.start();
        xserver.startEventLoop();
    }

    /**
     * 启动x11服务
     */
    @Deprecated
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
    @Deprecated
    private static void startXServer_windows() {
        // 判断进程是否存在
        String[] processName = setting.x11Binary();
        if (ProcessUtil.isProcessRunning(processName)) {
            return;
        }
        // 寻找存在的二进制命令
        String bin = ShellX11Util.findExist(setting.x11WorkDir(), setting.x11Binary());
        if (bin == null) {
            MessageBox.warn(ShellI18nHelper.x11Tip1());
            return;
        }
        // 异步启动
        ThreadUtil.start(() -> {
            try {
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
    @Deprecated
    private static void startXServer_macos() {
        // 判断进程是否存在
        String[] processName = {"XQuartz"};
        if (ProcessUtil.isProcessRunning(processName)) {
            return;
        }
        // 寻找存在的二进制命令
        String bin = ShellX11Util.findExist(setting.x11WorkDir(), setting.x11Binary());
        if (bin == null) {
            MessageBox.warn(ShellI18nHelper.x11Tip1());
            return;
        }
        // 异步启动
        ThreadUtil.start(() -> {
            try {
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
    @Deprecated
    public synchronized static void stopXServer() {
        if (x11Process != null) {
            try {
                ProcessHandle handle = x11Process.toHandle();
                if (OSUtil.isWindows() || OSUtil.isLinux()) {
                    handle.destroy();
                } else {
                    ProcessUtil.killProcess("X11.bin");
                }
                x11Process = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 停止x11服务，内建
     */
    public synchronized static void stopXServer_built_in() {
        if (xserver != null) {
            if (xserver.isRunning()) {
                xserver.stop();
            }
            xserver = null;
        }
    }

    /**
     * 获取xserver状态
     *
     * @return 结果
     */
    public synchronized static int xServerStatus() {
        if (xserver == null) {
            return 0;
        }
        if (xserver.isRunning()) {
            return 1;
        }
        return 2;
    }

    /**
     * 获取xserver客户端
     *
     * @return 结果
     */
    public synchronized static int xServerClients() {
        if (xserver == null) {
            return 0;
        }
        return CollectionUtil.size(xserver.clients());
    }

    static {
        // 注册钩子
        RuntimeUtil.addShutdownHook(new Thread(ShellX11Manager::stopXServer_built_in));
    }
}
