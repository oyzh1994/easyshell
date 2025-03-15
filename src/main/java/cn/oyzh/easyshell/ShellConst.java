package cn.oyzh.easyshell;

import cn.oyzh.common.util.JarUtil;
import lombok.experimental.UtilityClass;

import java.io.File;

/**
 * ssh常量对象
 *
 * @author oyzh
 * @since 2023/08/16
 */
@UtilityClass
public class ShellConst {

//    /**
//     * 数据保存路径
//     */
//    public static final String STORE_PATH = System.getProperty("user.home") + File.separator + ".easyssh_dev" + File.separator;

//    /**
//     * 缓存保存路径
//     */
//    public static final String CACHE_PATH = STORE_PATH + "cache" + File.separator;

    /**
     * icon地址
     */
    public final static String ICON_PATH = "/image/ssh_no_bg.png";

    /**
     * 托盘图标，windows专用
     */
    public final static String ICON_24_PATH = "/image/ssh_24.png";

    /**
     * 任务栏图标，windows专用
     */
    public final static String ICON_32_PATH = "/image/ssh_32.png";

    public static String getStorePath() {
        if (JarUtil.isInJar()) {
            return System.getProperty("user.home") + File.separator + ".easyssh_dev" + File.separator;
        }
        return System.getProperty("user.home") + File.separator + ".easyssh" + File.separator;
    }

    public static String getCachePath() {
        return getStorePath() + "cache" + File.separator;
    }

}
