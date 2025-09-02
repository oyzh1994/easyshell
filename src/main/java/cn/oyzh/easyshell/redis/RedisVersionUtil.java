package cn.oyzh.easyshell.redis;


import cn.oyzh.easyshell.command.RedisCommand;
import cn.oyzh.easyshell.command.RedisCommandUtil;
import cn.oyzh.easyshell.exception.redis.UnsupportedCommandException;

/**
 * redis版本工具类
 *
 * @author oyzh
 * @since 2023/07/31
 */

public class RedisVersionUtil {

    /**
     * 获取支持的版本
     *
     * @param command 指令
     * @return 支持的版本
     */
    public static String getSupportedVersion(String command) {
        RedisCommand redisCommand = RedisCommandUtil.getCommand(command);
        return redisCommand == null ? "1.0.0" : redisCommand.getAvailable();
    }

    /**
     * 检查指令是否支持
     *
     * @param serverVersion 服务版本
     * @param command       指令
     */
    public static void checkSupported(String serverVersion, String command) {
        String version = getSupportedVersion(command);
        if (!isSupported(serverVersion, version)) {
            throw new UnsupportedCommandException(serverVersion, version, command);
        }
    }

    /**
     * 判断指令是否支持
     *
     * @param serverVersion 服务版本
     * @param command       指令
     */
    public static boolean isCommandSupported(String serverVersion, String command) {
        String version = getSupportedVersion(command);
        return isSupported(serverVersion, version);
    }

    /**
     * 判断是否支持
     *
     * @param serverVersion 服务版本
     * @param version       指令版本
     * @return 结果
     */
    public static boolean isSupported(String serverVersion, String version) {
        if (serverVersion != null && version != null) {
            try {
                String[] str1 = serverVersion.split("\\.");
                String[] str2 = version.split("\\.");
                if (str2.length != 3 && str1.length != str2.length) {
                    return false;
                }
                String s1 = str1[0];
                String s11 = str2[0];
                if (Double.parseDouble(s1) > Double.parseDouble(s11)) {
                    return true;
                }
                String s2 = str1[1];
                String s22 = str2[1];
                if (Double.parseDouble(s2) > Double.parseDouble(s22)) {
                    return true;
                }
                String s3 = str1[2];
                String s33 = str2[2];
                return Double.parseDouble(s3) >= Double.parseDouble(s33);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
