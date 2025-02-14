package cn.oyzh.easyssh;

import lombok.experimental.UtilityClass;

import java.io.File;

/**
 * ssh常量对象
 *
 * @author oyzh
 * @since 2023/08/16
 */
@UtilityClass
public class SSHConst {

    /**
     * fxml基础地址
     */
    public final static String FXML_BASE_PATH = "/fxml/";

    /**
     * 数据保存路径
     */
    public static final String STORE_PATH = System.getProperty("user.home") + File.separator + ".easyssh" + File.separator;

    /**
     * 缓存保存路径
     */
    public static final String CACHE_PATH = STORE_PATH + "cache" + File.separator;

    /**
     * icon地址
     */
    public final static String ICON_PATH = "/image/ssh_clip.png";

}
