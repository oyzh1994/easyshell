package cn.oyzh.easyshell.redis;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.easyshell.ShellConst;

import java.nio.charset.StandardCharsets;

/**
 * redis缓存工具类
 *
 * @author oyzh
 * @since 2024-11-25
 */

public class RedisCacheUtil {

    private static String baseDir(int hashCode) {
        return ShellConst.getKeyCachePath() + hashCode;
    }

    /**
     * 缓存数据
     *
     * @param hashCode hash码
     * @param value    数据
     * @param suffix   尾缀
     * @return 缓存结果
     */
    public static boolean cacheValue(int hashCode, Object value, String suffix) {
        if (value != null) {
            try {
                String baseDir = baseDir(hashCode);
                String fileName = baseDir + "." + suffix;
                FileUtil.touch(fileName);
                byte type = 0;
                byte[] bytes = new byte[0];
                if (value instanceof String s) {
                    bytes = s.getBytes(StandardCharsets.UTF_8);
                    type = 1;
                } else if (value instanceof byte[] s) {
                    bytes = s;
                    type = 2;
                }
                byte[] bytes1 = new byte[bytes.length + 1];
                ArrayUtil.copy(bytes, bytes1);
                bytes1[bytes.length] = type;
                FileUtil.writeBytes(bytes1, fileName);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return deleteValue(hashCode, suffix);
    }

    /**
     * 加载数据
     *
     * @param hashCode hash码
     * @param suffix   尾缀
     * @return 数据
     */
    public static Object loadValue(int hashCode, String suffix) {
        try {
            String baseDir = ShellConst.getKeyCachePath() + hashCode;
            String fileName = baseDir + "." + suffix;
            if (FileUtil.exist(fileName)) {
                byte[] bytes = FileUtil.readBytes(fileName);
                byte type = bytes[bytes.length - 1];
                byte[] bytes1 = ArrayUtil.copy(bytes, bytes.length - 1);
                if (type == 1) {
                    return new String(bytes1);
                }
                if (type == 2) {
                    return bytes1;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 删除数据
     *
     * @param hashCode hash码
     * @param suffix   尾缀
     */
    public static boolean deleteValue(int hashCode, String suffix) {
        try {
            String baseDir = baseDir(hashCode);
            String fileName = baseDir + "." + suffix;
            FileUtil.del(fileName);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 是否存在数据
     *
     * @param hashCode hash码
     * @param suffix   尾缀
     */
    public static boolean hasValue(int hashCode, String suffix) {
        try {
            String baseDir = baseDir(hashCode);
            String fileName = baseDir + "." + suffix;
            return FileUtil.exist(fileName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
