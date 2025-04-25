package cn.oyzh.easyshell.x11;

import cn.oyzh.common.file.FileUtil;

/**
 * x11工具类
 *
 * @author oyzh
 * @since 2025/03/09
 */

public class ShellX11Util {

    public static String findExist(String workdir, String[] x11Binary) {
        return findExist(workdir, "/", x11Binary);
    }

    public static String findExist(String workdir, String midDir, String[] x11Binary) {
        String binExist = null;
        for (String bin : x11Binary) {
            if (FileUtil.exist(workdir + midDir + bin)) {
                binExist = bin;
                break;
            }
        }
        return binExist;
    }
}
