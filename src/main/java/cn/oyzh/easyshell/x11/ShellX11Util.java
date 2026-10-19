package cn.oyzh.easyshell.x11;

import cn.oyzh.common.file.FileUtil;

/**
 * x11工具类
 *
 * @author oyzh
 * @since 2025-03-09
 */
public class ShellX11Util {

    /**
     * 查找存在的x11二进制命令
     *
     * @param workdir  工作目录
     * @param x11Binary x11二进制命令
     * @return 存在的二进制命令，不存在时返回 null
     */
    @Deprecated
    public static String findExist(String workdir, String[] x11Binary) {
        return findExist(workdir, "/", x11Binary);
    }

    /**
     * 查找存在的x11二进制命令
     *
     * @param workdir  工作目录
     * @param midDir   中间目录
     * @param x11Binary x11二进制命令
     * @return 存在的二进制命令，不存在时返回 null
     */
    @Deprecated
    public static String findExist(String workdir, String midDir, String[] x11Binary) {
        String binExist = null;
        for (String bin : x11Binary) {
            if (FileUtil.exists(workdir + midDir + bin)) {
                binExist = bin;
                break;
            }
        }
        return binExist;
    }
}
