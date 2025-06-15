package cn.oyzh.easyshell.s3;

/**
 * s3工具类
 *
 * @author oyzh
 * @since 2025-06-14
 */
public class ShellS3Util {

    /**
     * 转换为s3前缀
     *
     * @param fPath 文件路径
     * @return s3前缀
     */
    public static String toPrefix(String fPath) {
        String fPrefix;
        if ("/".equals(fPath)) {
            fPrefix = "";
        } else if (!fPath.endsWith("/")) {
            if (fPath.startsWith("/")) {
                fPrefix = fPath.substring(1) + "/";
            } else {
                fPrefix = fPath + "/";
            }
        } else {
            fPrefix = fPath;
        }
        return fPrefix;
    }
}
