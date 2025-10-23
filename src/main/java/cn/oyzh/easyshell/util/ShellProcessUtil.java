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
               //  JulLog.info("running in AppImage...");
               //  String appImagePath = System.getenv("APPIMAGE");
               //  // 工作目录
               //  File dir = new File(appImagePath).getParentFile();
               //  // 构建重启命令
               //  // String[] cmd = { "nohup", appImagePath, "&"};
               //  String[] cmd = {"setsid", "nohup", appImagePath, "&"};
               //  ProcessBuilder builder = new ProcessBuilder(cmd);
               //  // ProcessBuilder builder = new ProcessBuilder("sh", "-c", "nohup \"" + appImagePath + "\" &");
               //  // 重定向输入输出到/dev/null或日志文件
               //  builder.redirectError(new File("/dev/null"));
               //  builder.redirectOutput(new File("/dev/null"));
               //  // ProcessBuilder  builder = new ProcessBuilder("nohup", appImagePath, "&","disown");
               //  // Map<String, String> env = builder.environment();
               //  // env.put("LD_LIBRARY_PATH", "/path/to/appimage/libs:" + env.getOrDefault("LD_LIBRARY_PATH", ""));
               //  // 设置运行目录
               //  builder.directory(dir);
               //  JulLog.info("running in AppImage...");
               // Process process= builder.start();
               //
               // process.waitFor();
               //  // builder.redirectErrorStream(true);
               //  // builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
               //  StageManager.exit();
               //  restartWithDelay();
                restartWithDelay1();
                return;
            }
            ProcessUtil.restartApplication2(100, StageManager::exit);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void restartWithDelay() {
        try {
            String appImagePath = System.getenv("APPIMAGE");

            // 使用 sleep 10 来延迟执行
            String cmd = String.format(
                    "sh -c 'sleep 5; nohup \"%s\" > /dev/null 2>&1 &'",
                    appImagePath
            );

            ProcessBuilder pb = new ProcessBuilder("sh", "-c", cmd);
            Process process = pb.start();

            // 不要等待这个进程结束，我们让它后台运行
            // 然后退出当前程序
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void restartWithDelay1() {
        try {
            String appImagePath = System.getenv("APPIMAGE");

            // 使用setsid创建一个新的会话，然后执行延迟启动
            String[] cmd = {
                    "setsid", "sh", "-c",
                    "sleep 10; nohup \"" + appImagePath + "\" > /dev/null 2>&1 &"
            };

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.start();

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
