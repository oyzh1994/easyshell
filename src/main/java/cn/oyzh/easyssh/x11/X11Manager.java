package cn.oyzh.easyssh.x11;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.ProcessUtil;
import cn.oyzh.common.system.RuntimeUtil;
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

    private static Process x11Process;

    public static void startXServer() {
        if (OSUtil.isWindows()) {
            // 杀死可能的旧进程
            if (x11Process == null) {
                String processName = "vcxsrv.exe";
                if (ProcessUtil.isProcessRunning(processName)) {
                    ProcessUtil.killProcess(processName);
                }
            }
            String dir = ResourceUtil.getResource("/bin/vcxsrv/").getFile();
            if (dir.startsWith("/")) {
                dir = dir.substring(1);
            }
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
            x11Process = RuntimeUtil.exec(command.toString(), null, new File(dir));
            // 注册钩子
            RuntimeUtil.addShutdownHook(new Thread(X11Manager::stopXServer));
        }
    }

    public static void stopXServer() {
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
