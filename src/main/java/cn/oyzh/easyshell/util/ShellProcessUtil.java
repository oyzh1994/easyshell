package cn.oyzh.easyshell.util;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.ProcessUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.window.StageManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
            if (ProcessUtil.isRunningInAppImage()) {
                String appImagePath = System.getenv("APPIMAGE");
                JulLog.info("appImagePath:" + appImagePath);
                String javaPath = System.getProperty("java.home");
                String classPath = System.getProperty("java.class.path");
                String appDir = "";
                String appDirBak = "";
                for (String s : classPath.split("/")) {
                    if (StringUtil.isNotBlank(s)) {
                        appDir += "/" + s;
                    }
                    if (s.startsWith(".mount_")) {
                        appDirBak = appDir + "_bak";
                        break;
                    }
                }
                FileUtil.copyDirectory(appDir, appDirBak);
                javaPath = javaPath.replace(appDir, appDirBak);
                classPath = classPath.replace(appDir, appDirBak);
                JulLog.info("appDir:{}", appDir);
                JulLog.info("classPath:{}", classPath);
                JulLog.info("javaPath:{}", javaPath);
                if (!FileUtil.exist(javaPath + "/bin/javaw")) {
                    javaPath += "/bin/java";
                } else {
                    javaPath += "/bin/javaw";
                }
                // 工作目录
                File dir = new File(javaPath).getParentFile();
                // 构建重启命令
                ProcessBuilder builder = new ProcessBuilder("nohup", javaPath, "-jar", classPath, "&");
                // 设置运行目录
                builder.directory(dir);
                // 打印命令
                JulLog.info("restartCommand:{} dir:{}", Arrays.toString(builder.command().toArray()), dir);
                // 执行重启命令
                builder.start();

                StageManager.exit();
                return;
            }
            // if (ProcessUtil.isRunningInAppImage()) {
            // ProcessUtil.restartApplication2();
//                 String appImagePath = System.getenv("APPIMAGE");
//                JulLog.info("appImagePath:" + appImagePath);
//                 ProcessBuilder pb = new ProcessBuilder("sh", "-c", "nohup " + appImagePath + " &");
// // 清理环境，只传递必要的变量
//                 Map<String, String> env = pb.environment();
//                 env.clear();
//                 env.put("PATH", "/usr/local/bin:/usr/bin:/bin");
// //                 env.put("DISPLAY", System.getenv("DISPLAY")); // 如果是GUI应用
//                 pb.start();
//                 String[] cmd = { "nohup", appImagePath, "&"};
//                 Restarter.restart(cmd);
//                 StageManager.exit();
//                 return;
//             }
            // // 运行在appImage格式中
            // if (ProcessUtil.isRunningInAppImage()) {
            //    //  JulLog.info("running in AppImage...");
            //    //  String appImagePath = System.getenv("APPIMAGE");
            //    //  // 工作目录
            //    //  File dir = new File(appImagePath).getParentFile();
            //    //  // 构建重启命令
            //    //  // String[] cmd = { "nohup", appImagePath, "&"};
            //    //  String[] cmd = {"setsid", "nohup", appImagePath, "&"};
            //    //  ProcessBuilder builder = new ProcessBuilder(cmd);
            //    //  // ProcessBuilder builder = new ProcessBuilder("sh", "-c", "nohup \"" + appImagePath + "\" &");
            //    //  // 重定向输入输出到/dev/null或日志文件
            //    //  builder.redirectError(new File("/dev/null"));
            //    //  builder.redirectOutput(new File("/dev/null"));
            //    //  // ProcessBuilder  builder = new ProcessBuilder("nohup", appImagePath, "&","disown");
            //    //  // Map<String, String> env = builder.environment();
            //    //  // env.put("LD_LIBRARY_PATH", "/path/to/appimage/libs:" + env.getOrDefault("LD_LIBRARY_PATH", ""));
            //    //  // 设置运行目录
            //    //  builder.directory(dir);
            //    //  JulLog.info("running in AppImage...");
            //    // Process process= builder.start();
            //    //
            //    // process.waitFor();
            //    //  // builder.redirectErrorStream(true);
            //    //  // builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            //    //  StageManager.exit();
            //    //  restartWithDelay();
            //     restartWithDelay1();
            //     return;
            // }
            ProcessUtil.restartApplication2(100, StageManager::exit);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // public static void restartWithDelay() {
    //     try {
    //         String appImagePath = System.getenv("APPIMAGE");
    //
    //         // 使用 sleep 10 来延迟执行
    //         String cmd = String.format(
    //                 "sh -c 'sleep 5; nohup \"%s\" > /dev/null 2>&1 &'",
    //                 appImagePath
    //         );
    //
    //         ProcessBuilder pb = new ProcessBuilder("sh", "-c", cmd);
    //         Process process = pb.start();
    //
    //         // 不要等待这个进程结束，我们让它后台运行
    //         // 然后退出当前程序
    //         System.exit(0);
    //
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
    //
    // public static void restartWithDelay1() {
    //     try {
    //         String appImagePath = System.getenv("APPIMAGE");
    //
    //         // 使用setsid创建一个新的会话，然后执行延迟启动
    //         String[] cmd = {
    //                 "setsid", "sh", "-c",
    //                 "sleep 10; nohup \"" + appImagePath + "\" > /dev/null 2>&1 &"
    //         };
    //
    //         ProcessBuilder pb = new ProcessBuilder(cmd);
    //         pb.start();
    //
    //         System.exit(0);
    //
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    // public static void restartAppImageWithDisplay() {
    //     try {
    //         // 1. 获取AppImage的路径和当前显示环境
    //         String appImagePath = System.getenv("APPIMAGE");
    //         String displayEnv = System.getenv("DISPLAY"); // 使用诊断到的值
    //         String xauthorityEnv = System.getenv("XAUTHORITY");
    //
    //         File dir=new File(appImagePath).getParentFile();
    //         // 2. 使用ProcessBuilder构建新的进程
    //         ProcessBuilder pb = new ProcessBuilder();
    //
    //         // 设置命令 - 这里强烈建议先提取再运行，或使用包装脚本
    //         // 示例：通过shell命令来延迟启动并明确设置环境
    //         String[] shellCmd = {
    //                 "sh", "-c",
    //                 "sleep 2 && nohup \"" + appImagePath + "\" > /dev/null 2>&1 &"
    //         };
    //         pb.command(shellCmd);
    //
    //         // 3. ！！！关键步骤：设置子进程的环境变量！！！
    //         Map<String, String> env = pb.environment();
    //         // 清除可能受限的旧环境（可选，但常能解决问题）
    //         // env.clear();
    //         // 重新设置必要的环境变量
    //         env.put("DISPLAY", displayEnv != null ? displayEnv : ":0"); // 使用诊断到的值，缺省为:0
    //         if (xauthorityEnv != null) {
    //             env.put("XAUTHORITY", xauthorityEnv);
    //         }
    //         env.put("PATH", "/usr/local/bin:/usr/bin:/bin"); // 设置一个基础PATH
    //
    //         // 4. 设置工作目录（可选，建议设置为用户目录或AppImage所在目录）
    //         pb.directory(dir);
    //         // pb.directory(new File(System.getProperty("user.home")));
    //
    //         // 5. 启动进程并退出当前应用
    //         Process process = pb.start();
    //         System.exit(0);
    //
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         // 在这里可以添加回退方案，例如尝试不带nohup直接启动，或者给用户一个错误提示
    //     }
    // }
}
