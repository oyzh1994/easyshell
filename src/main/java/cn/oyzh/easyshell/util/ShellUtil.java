package cn.oyzh.easyshell.util;

import cn.oyzh.common.util.StringUtil;

/**
 * @author oyzh
 * @since 2025-03-26
 */
public class ShellUtil {

    public static boolean isCommandNotFound(String output) {
        return StringUtil.containsAnyIgnoreCase(output, "not found", "未找到命令");
    }

    public static boolean hasOwnerReadPermission(String permission) {
        char[] chars = permission.toCharArray();
        return chars.length >= 2 && chars[1] == 'r';
    }
}
