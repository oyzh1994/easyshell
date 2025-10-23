package cn.oyzh.easyshell.util;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.ProcessUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.window.StageManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author oyzh
 * @since 2025-03-17
 */
public class ShellProcessUtil {

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
