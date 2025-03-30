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

    public static boolean isWindowsCommandNotFound(String output, String cmd) {
        return StringUtil.containsIgnoreCase(output, "'" + cmd + "'");
    }

    public static String fixWindowsFilePath(String filePath) {
        return StringUtil.replace(filePath, "/", "\\");
    }

    public static String reverseWindowsFilePath(String filePath) {
        return StringUtil.replace(filePath, "\\", "/");
    }

    public static boolean hasOwnerReadPermission(String permission) {
        char[] chars = permission.toCharArray();
        return chars.length >= 2 && chars[1] == 'r';
    }

    public static boolean hasOwnerWritePermission(String permission) {
        char[] chars = permission.toCharArray();
        return chars.length >= 3 && chars[2] == 'w';
    }

    public static boolean hasOwnerExecutePermission(String permission) {
        char[] chars = permission.toCharArray();
        return chars.length >= 4 && chars[3] == 'x';
    }

    public static boolean hasGroupsReadPermission(String permission) {
        char[] chars = permission.toCharArray();
        return chars.length >= 5 && chars[4] == 'r';
    }

    public static boolean hasGroupsWritePermission(String permission) {
        char[] chars = permission.toCharArray();
        return chars.length >= 6 && chars[5] == 'w';
    }

    public static boolean hasGroupsExecutePermission(String permission) {
        char[] chars = permission.toCharArray();
        return chars.length >= 7 && chars[6] == 'x';
    }

    public static boolean hasOthersReadPermission(String permission) {
        char[] chars = permission.toCharArray();
        return chars.length >= 8 && chars[7] == 'r';
    }

    public static boolean hasOthersWritePermission(String permission) {
        char[] chars = permission.toCharArray();
        return chars.length >= 9 && chars[8] == 'w';
    }

    public static boolean hasOthersExecutePermission(String permission) {
        char[] chars = permission.toCharArray();
        return chars.length >= 10 && chars[9] == 'x';
    }

    public static String permission(String permission) {
        int[] permissions = new int[3];
        for (int i = 0; i < 3; i++) {
            int start = i * 3;
            int octal = 0;
            if (permission.charAt(start) == 'r') {
                octal += 4;
            }
            if (permission.charAt(start + 1) == 'w') {
                octal += 2;
            }
            if (permission.charAt(start + 2) == 'x') {
                octal += 1;
            }
            permissions[i] = octal;
        }
        return permissions[0] + "" + permissions[1] + permissions[2];
    }

    public static int permissionToInt(String permission) {
        int[] permissions = new int[3];
        for (int i = 0; i < 3; i++) {
            int start = i * 3;
            int octal = 0;
            if (permission.charAt(start) == 'r') {
                octal += 4;
            }
            if (permission.charAt(start + 1) == 'w') {
                octal += 2;
            }
            if (permission.charAt(start + 2) == 'x') {
                octal += 1;
            }
            permissions[i] = octal;
        }
        String str = "0" + permissions[0] + permissions[1] + permissions[2];
        return Integer.parseInt(str, 8);
    }

    public static String getWindowsCommandResult(String output) {
        if (StringUtil.isBlank(output)) {
            return "";
        }
        String[] arr = output.split("\n");
        if (arr.length < 2) {
            return "";
        }
        output = arr[1];
        return output.trim();
    }
}
