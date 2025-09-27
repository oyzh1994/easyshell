package cn.oyzh.easyshell;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.JarUtil;

import java.io.File;

/**
 * shell常量对象
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class ShellConst {

    // /**
    //  * sftp组件是否可见
    //  */
    // public final static String SFTP_VISIBLE = "sftp:visible";

    /**
     * icon地址
     */
    public final static String ICON_PATH = "/image/shell_no_bg.png";

    /**
     * 托盘图标，windows专用
     */
    public final static String ICON_24_PATH = "/image/shell_24.png";

    /**
     * 任务栏图标，windows专用
     */
    public final static String ICON_32_PATH = "/image/shell_32.png";

    /**
     * 获取存储路径
     *
     * @return 存储路径
     */
    public static String getStorePath() {
        if (JarUtil.isInJar()) {
            return SystemUtil.userHome() + File.separator + ".easyshell" + File.separator;
        }
        return SystemUtil.userHome() + File.separator + ".easyshell_dev" + File.separator;
    }

    /**
     * 获取缓存路径
     *
     * @return 缓存路径
     */
    public static String getCachePath() {
        return getStorePath() + "cache" + File.separator;
    }

    // public static boolean isSftpVisible() {
    //     return System.getProperty(SFTP_VISIBLE) != null;
    // }

    /**
     * 获取键缓存路径
     *
     * @return 键缓存路径
     */
    public static String getKeyCachePath() {
        return getCachePath() + "key_cache" + File.separator;
    }

    /**
     * 获取节点缓存路径
     *
     * @return 节点缓存路径
     */
    public static String getNodeCachePath() {
        return getCachePath() + "node_cache" + File.separator;
    }
}
