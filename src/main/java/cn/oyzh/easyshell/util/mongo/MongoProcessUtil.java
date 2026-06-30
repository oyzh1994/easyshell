package cn.oyzh.easyshell.util.mongo;

import cn.oyzh.common.system.ProcessUtil;
import cn.oyzh.fx.plus.window.StageManager;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2024-12-18
 */
public class MongoProcessUtil {

    /**
     * 重启应用
     */
    public static void restartApplication() {
        try {
            ProcessUtil.restartApplication(100, StageManager::exit);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
