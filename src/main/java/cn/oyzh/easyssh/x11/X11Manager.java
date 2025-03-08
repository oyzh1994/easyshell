package cn.oyzh.easyssh.x11;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.ProcessUtil;
import cn.oyzh.common.system.RuntimeUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ResourceUtil;
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
     * x11进程
     */
    private static Process x11Process;

    /**
     * 启动x11服务
     */
    public synchronized static void startXServer() {
        if (x11Process != null && x11Process.isAlive()) {
            return;
        }
        // 判断进程是否存在
        String processName = "vcxsrv.exe";
        if (ProcessUtil.isProcessRunning(processName)) {
            return;
        }
        if (OSUtil.isWindows()) {
            // 异步启动
            ThreadUtil.start(() -> {
                StringBuilder command = new StringBuilder("cmd.exe /c start vcxsrv.exe");
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
                // vcxsrv服务地址
                String dir = ResourceUtil.getResource("/bin/vcxsrv/").getFile();
                if (dir.startsWith("/")) {
                    dir = dir.substring(1);
                }
                x11Process = RuntimeUtil.exec(command.toString(), null, new File(dir));
                // 注册钩子
                RuntimeUtil.addShutdownHook(new Thread(X11Manager::stopXServer));
            });
        }
    }

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
}
