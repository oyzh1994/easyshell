package cn.oyzh.easyshell.file;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.util.StringUtil;

public class ShellFileUtil {

    /**
     * 是否正常文件
     *
     * @param fileName 文件名
     * @return 结果
     */
    public static boolean isNormal(String fileName) {
        return !StringUtil.equalsAny(fileName, ".", "..");
    }

    public static String parent(String path) {
        if (StringUtil.isEmpty(path)) {
            return path;
        }
        return path.substring(0, path.lastIndexOf("/"));
    }

    public static String name(String path) {
        if (StringUtil.isEmpty(path)) {
            return path;
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static String concat(String src, String name) {
        src = src.replace("\\", "/");
        name = name.replace("\\", "/");
        String path;
        if (src.endsWith("/") && name.startsWith("/")) {
            path = src + name.substring(1);
        } else if (!src.endsWith("/") && !name.startsWith("/")) {
            path = src + "/" + name;
        } else {
            path = src + name;
        }
        return path;
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

    /**
     * 文件是否可编辑
     *
     * @param file 文件
     * @return 结果
     */
    public static boolean fileEditable(ShellFile file) {
        if (!file.isFile()) {
            return false;
        }
        if (file.getFileSize() > 500 * 1024) {
            return false;
        }
        // 检查类型
        String extName = FileNameUtil.extName(file.getFileName());
        return StringUtil.equalsAnyIgnoreCase(extName,
                "txt", "text", "log", "yaml", "java",
                "xml", "json", "htm", "html", "xhtml",
                "php", "css", "c", "cpp", "rs",
                "js", "csv", "sql", "md", "ini",
                "cfg", "sh", "bat", "py", "asp",
                "aspx", "env", "tsv", "conf");
    }

//    /**
//     * 将Unix风格的权限字符串（如"drwx------"）转换为数字表示（如700）
//     *
//     * @param permissions 权限字符串
//     * @return 数字表示的权限
//     */
//    public static int toPermissionInt(String permissions) {
//        // 无效权限字符串
//        if (permissions == null || permissions.length() < 9) {
//            return 0;
//        }
//
//        // 忽略第一个字符（文件类型）
//        String permission = permissions.substring(1, 10);
//        if (permissions.length() == 10) {
//            permission = permissions.substring(1, 10);
//        } else {
//            permission = permissions;
//        }
//
//        // 计算三组权限值
//        int owner = calculatePermissionGroup(permission.substring(0, 3));
//        int group = calculatePermissionGroup(permission.substring(3, 6));
//        int others = calculatePermissionGroup(permission.substring(6, 9));
//
//        // 组合为三位数整数
//        return owner * 100 + group * 10 + others;
//    }

//    private static int calculatePermissionGroup(String group) {
//        int value = 0;
//        if (group.charAt(0) == 'r') value += 4;
//        if (group.charAt(1) == 'w') value += 2;
//        if (group.charAt(2) == 'x') value += 1;
//        return value;
//    }

    /**
     * 将Unix风格的权限字符串（如"drwx------"）转换为数字表示（如700）
     *
     * @param permission 权限字符串
     * @return 数字表示的权限
     */
    public static int toPermissionInt(String permission) {
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

    /**
     * 对路径进行修正
     *
     * @param filePath 路径
     * @return 修正后的路径
     */
    public static String fixFilePath(String filePath) {
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }
        filePath = StringUtil.replace(filePath, "\\", "/");
        return StringUtil.replace(filePath, "//", "/");
    }

    /**
     * 针对windows路径做出来
     *
     * @param filePath 文件路径
     * @return 结果
     */
    public static String fixWindowsFilePath(String filePath) {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        return StringUtil.replace(filePath, "/", "\\");
    }
}
