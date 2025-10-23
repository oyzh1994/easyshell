package cn.oyzh.easyshell.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.ProcessUtil;
import cn.oyzh.fx.plus.window.StageManager;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author oyzh
 * @since 2025-03-17
 */
public class ShellProcessUtil {

    /**
     * 重启应用
     */
    public static void restartApplication() {
        try {
            // 运行在appImage格式中
            if (ProcessUtil.isRunningInAppImage()) {
                JulLog.info("running in AppImage...");
                String appImagePath = System.getenv("APPIMAGE");
                // 工作目录
                File dir = new File(appImagePath).getParentFile();
                // 构建重启命令
                ProcessBuilder builder = new ProcessBuilder("sh", "-c", "nohup \"" + appImagePath + "\" &");
                // ProcessBuilder  builder = new ProcessBuilder("nohup", appImagePath, "&","disown");
                // Map<String, String> env = builder.environment();
                // env.put("LD_LIBRARY_PATH", "/path/to/appimage/libs:" + env.getOrDefault("LD_LIBRARY_PATH", ""));
                // 设置运行目录
                builder.directory(dir);
                JulLog.info("running in AppImage...");
                builder.start();
                return;
            }
            ProcessUtil.restartApplication2(100, StageManager::exit);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
