package cn.oyzh.easyshell.terminal.zk;

/**
 * zk终端工具
 *
 * @author oyzh
 * @since 2023/09/20
 */

public class ZKTerminalUtil {

    /**
     * 获取路径
     *
     * @param input 输入
     * @return 路径
     */
    public static String getPath(String input) {
        String[] strArr = input.split(" ");
        for (String str : strArr) {
            if (str.startsWith("-")) {
                continue;
            }
            if (str.startsWith("/")) {
                return str;
            }
        }
        return null;
    }
}
