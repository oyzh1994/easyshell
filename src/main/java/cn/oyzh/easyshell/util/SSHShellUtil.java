//package cn.oyzh.easyshell.util;
//
//import cn.oyzh.common.thread.ThreadUtil;
//import cn.oyzh.common.util.ArrayUtil;
//import lombok.experimental.UtilityClass;
//
//import java.io.IOException;
//import java.io.InputStream;
//
///**
// * shell交互式终端工具类
// *
// * @author oyzh
// * @since 2023/08/17
// */
//
//public class SSHShellUtil {
//
//    /**
//     * 读取结果
//     *
//     * @param in 流
//     * @return 结果
//     */
//    public static String readInput(InputStream in) throws IOException {
//        return readInput(in, 20, 1000);
//    }
//
//    /**
//     * 读取结果
//     *
//     * @param in        流
//     * @param firstWait 首次等待时间
//     * @param maxWait   最大等待时间
//     * @return 结果
//     */
//    public static String readInput(InputStream in, int firstWait, int maxWait) throws IOException {
//        if (firstWait > 0) {
//            ThreadUtil.sleep(firstWait);
//        }
//        // 获取命令执行结果
//        StringBuilder builder = new StringBuilder();
//        // 开始时间
//        long start = System.currentTimeMillis();
//        // 临时数组
//        byte[] tmp = new byte[1024];
//        // 最后行
//        String line = null;
//        // 读取流数据
//        while (true) {
//            // 分批读取流
//            while (in.available() > 0) {
//                int n = in.read(tmp, 0, tmp.length);
//                if (n <= 0) {
//                    break;
//                }
//                line = new String(tmp, 0, n);
//                builder.append(line);
//            }
//            // 数据不为空，则直接结束
//            if (line != null) {
//                break;
//            }
//            // 超时
//            long current = System.currentTimeMillis();
//            if (current - start > maxWait) {
//                break;
//            }
//            ThreadUtil.sleep(10);
//        }
//        return builder.toString();
//    }
//
//    /**
//     * 是否提示符
//     *
//     * @param str 内容
//     * @return 结果
//     */
//    public static boolean isPrompt(String str) {
//        if (str == null || str.isEmpty()) {
//            return false;
//        }
//        str = str.trim();
//        return str.startsWith("[") && str.contains("@") && str.contains("]");
//    }
//
//    /**
//     * 是否有提示符
//     *
//     * @param str 内容
//     * @return 结果
//     */
//    public static boolean hasPrompt(String str) {
//        if (str == null || str.isEmpty()) {
//            return false;
//        }
//        return isPrompt(ArrayUtil.last(str.split("\n")));
//    }
//}
