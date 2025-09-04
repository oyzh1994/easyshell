package cn.oyzh.easyshell.util.zk;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.easyshell.ShellConst;

import java.io.File;

/**
 * zk缓存工具类
 *
 * @author oyzh
 * @since 2024-11-25
 */

public class ZKCacheUtil {

    /**
     * 获取基础路径
     *
     * @param hashCode hash码
     * @return 基础路径
     */
    private static String baseDir(int hashCode) {
        return ShellConst.getNodeCachePath() + hashCode + "_";
    }

    /**
     * 缓存数据
     *
     * @param hashCode hash码
     * @param data     数据
     * @param suffix   尾缀
     * @return 结果
     */
    public static boolean cacheData(int hashCode, byte[] data, String suffix) {
        if (data != null) {
            try {
                String baseDir = baseDir(hashCode);
                String fileName = baseDir + "." + suffix;
                FileUtil.touch(fileName);
                FileUtil.writeBytes(data, fileName);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return deleteData(hashCode, suffix);
    }

    /**
     * 加载数据
     *
     * @param hashCode hash码
     * @param suffix   尾缀
     * @return 数据
     */
    public static byte[] loadData(int hashCode, String suffix) {
        try {
            String baseDir = baseDir(hashCode);
            String fileName = baseDir + "." + suffix;
            if (FileUtil.exist(fileName)) {
                return FileUtil.readBytes(fileName);
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
    public static boolean deleteData(int hashCode, String suffix) {
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
     * 是否有数据
     *
     * @param hashCode hash码
     * @param suffix   尾缀
     * @return 结果
     */
    public static boolean hasData(int hashCode, String suffix) {
        return dataSize(hashCode, suffix) > 0;
    }

    /**
     * 获取数据长度
     *
     * @param hashCode hash码
     * @param suffix   尾缀
     * @return 数据长度
     */
    public static long dataSize(int hashCode, String suffix) {
        try {
            String baseDir = baseDir(hashCode);
            String fileName = baseDir + "." + suffix;
            File file = new File(fileName);
            if (file.exists()) {
                return file.length();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }
}
