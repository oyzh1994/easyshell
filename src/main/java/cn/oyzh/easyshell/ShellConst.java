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

    public static String getStorePath() {
        if (JarUtil.isInJar()) {
            return System.getProperty("user.home") + File.separator + ".easyshell_dev" + File.separator;
        }
        return System.getProperty("user.home") + File.separator + ".easyshell" + File.separator;
    }

    public static String getCachePath() {
        return getStorePath() + "cache" + File.separator;
    }

}
