package cn.oyzh.easyssh.util;

import cn.oyzh.common.system.ProcessUtil;
import cn.oyzh.fx.plus.window.StageManager;
import lombok.experimental.UtilityClass;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2024-12-17
 */
@UtilityClass
public class SSHProcessUtil {

    /**
     * 重启应用
     */
    public static void restartApplication() {
        try {
            ProcessUtil.restartApplication2(100, StageManager::exit);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
