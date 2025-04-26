package cn.oyzh.easyshell.util;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.util.StringUtil;

public class ShellFileUtil {

    public static String parent(String dest) {
        if (StringUtil.isEmpty(dest)) {
            return dest;
        }
        return dest.substring(0, dest.lastIndexOf("/"));
    }

    public static String concat(String src, String name) {
        if (src.endsWith("/") && name.startsWith("/")) {
            return src + name.substring(1);
        }
        if (!src.endsWith("/") && !name.startsWith("/")) {
            return src + "/" + name;
        }
        return src + name;
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
}
