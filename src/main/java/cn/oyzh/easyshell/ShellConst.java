package cn.oyzh.easyshell;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.JarUtil;

import java.io.File;

/**
 * shell常量对象
 *
 * @author oyzh
 * @since 2023-08-16
 */
public class ShellConst {

    // /**
    //  * sftp组件是否可见
    //  */
    // public final static String SFTP_VISIBLE = "sftp:visible";

    /**
     * icon地址
     */
    public final static String ICON_PATH = "/image/app_square_no_bg.png";
//    public final static String ICON_PATH = "/image/shell_no_bg.png";

    /**
     * 托盘图标，windows专用
     */
    @Deprecated
    public final static String ICON_24_PATH = "/image/app_square_24.png";
//    public final static String ICON_24_PATH = "/image/shell_24.png";

    /**
     * 任务栏图标，windows专用
     */
    public final static String ICON_32_PATH = "/image/app_square_32.png";
//    public final static String ICON_32_PATH = "/image/shell_32.png";

//    /**
//     * 初始化jfx缓存目录
//     */
//    public static void initJfxCache() {
//        String snapUserData = System.getenv("SNAP_USER_DATA");
//        if (snapUserData != null) {
//            System.setProperty("javafx.cachedir", File.separator + "/jfx");
//        }
//    }

    /**
     * 获取存储路径
     *
     * @return 存储路径
     */
    public static String getStorePath() {
        if (SystemUtil.isProd()) {
//            String snapUserData = System.getenv("SNAP_USER_DATA");
//            if (snapUserData != null) {
//                return snapUserData + File.separator + "easyshell" + File.separator;
//            }
            return SystemUtil.userHome() + File.separator + ".easyshell" + File.separator;
        }
        return SystemUtil.userHome() + File.separator + ".easyshell_dev" + File.separator;
    }

    /**
     * 获取skills存储路径
     *
     * @return skills存储路径
     */
    public static String getSkillPath() {
        if (SystemUtil.isProd()) {
            return getStorePath() + "skills" + File.separator;
        }
        return getStorePath() + "skills" + File.separator;
    }

//    /**
//     * 获取存储路径
//     *
//     * @return 存储路径
//     */
//    public static String getTempPath() {
//        if (SystemUtil.isProd()) {
//            return SystemUtil.tmpdir() + File.separator + "easyshell" + File.separator;
//        }
//        return SystemUtil.tmpdir() + File.separator + "easyshell_dev" + File.separator;
//    }

    /**
     * 获取缓存路径
     *
     * @return 缓存路径
     */
    public static String getCachePath() {
        return getStorePath() + "cache" + File.separator;
    }

    /**
     * 获取sqlite原生库路径
     *
     * @return 存储路径
     */
    public static String getSqliteNativePath() {
        return getStorePath() + "sqlite-native" + File.separator;
    }

    /**
     * 获取skills释放缓存路径
     *
     * @return skills释放缓存路径
     */
    public static String getSkillCachePath() {
        return getCachePath() + "skills" + File.separator;
    }

    /**
     * 获取键缓存路径
     *
     * @return 键缓存路径
     */
    public static String getKeyCachePath() {
        return getCachePath() + "key_cache" + File.separator;
    }

    /**
     * 获取键缓存路径
     *
     * @return 键缓存路径
     */
    @Deprecated
    public static String getKeyCachePath_old() {
        return getStorePath() + "key_cache" + File.separator;
    }

    /**
     * 获取节点缓存路径
     *
     * @return 节点缓存路径
     */
    public static String getNodeCachePath() {
        return getCachePath() + "node_cache" + File.separator;
    }

    /**
     * 获取节点缓存路径
     *
     * @return 节点缓存路径
     */
    @Deprecated
    public static String getNodeCachePath_old() {
        return getStorePath() + "node_cache" + File.separator;
    }
}
