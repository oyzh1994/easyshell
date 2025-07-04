package cn.oyzh.easyshell.file;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellFileCollect;
import cn.oyzh.easyshell.store.ShellFileCollectStore;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件工具类
 *
 * @author oyzh
 * @since 2025-04-28
 */
public class ShellFileUtil {

    /**
     * 文件收藏存储
     */
    private static final ShellFileCollectStore FILE_COLLECT_STORE = ShellFileCollectStore.INSTANCE;

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
                "aspx", "env", "tsv", "conf",
                "plist"
        );
    }

   /**
    * 将Unix风格的权限字符串（如"drwx------"）转换为数字表示（如700）
    *
    * @param permissions 权限字符串
    * @return 数字表示的权限
    */
   public static int toPermissionInt(String permissions) {
       // 无效权限字符串
       if (permissions == null || permissions.length() < 9) {
           return 0;
       }

       // 忽略第一个字符（文件类型）
       if (permissions.length() == 10) {
           permissions = permissions.substring(1);
       }

       // 计算三组权限值
       int owner = calculatePermissionGroup(permissions.substring(0, 3));
       int group = calculatePermissionGroup(permissions.substring(3, 6));
       int others = calculatePermissionGroup(permissions.substring(6, 9));

       // 组合为三位数整数
       return owner * 100 + group * 10 + others;
   }

   private static int calculatePermissionGroup(String group) {
       int value = 0;
       if (group.charAt(0) == 'r') value += 4;
       if (group.charAt(1) == 'w') value += 2;
       if (group.charAt(2) == 'x') value += 1;
       return value;
   }

    // /**
    //  * 将Unix风格的权限字符串（如"drwx------"）转换为数字表示（如700）
    //  *
    //  * @param permission 权限字符串
    //  * @return 数字表示的权限
    //  */
    // public static int toPermissionInt(String permission) {
    //     String str = rwxToOctal(permission);
    //     return Integer.parseInt(str, 8);
    // }

    /**
     * 将Unix风格的权限字符串（如"drwx------"）转换为字符表示（如700）
     *
     * @param permission 权限字符串
     * @return 数字表示的权限
     */
    public static String rwxToOctal(String permission) {
        // 如果是10位，则移除首位
        if (permission.length() == 10) {
            permission = permission.substring(1);
        } else if (permission.length() == 11) { // 如果是11位，则移除首位和末尾
            permission = permission.substring(1, permission.length() - 1);
        }
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
        return "0" + permissions[0] + permissions[1] + permissions[2];
    }

    /**
     * 将八进制权限数字（如 777）转换为 rwx 形式的9位字符串
     *
     * @param octalPermissions 八进制权限数字的字符串形式（如 "777"）
     * @return rwx 形式的权限字符串（如 "rwxrwxrwx"）
     */
    public static String octalToRwx(String octalPermissions) {
        if (octalPermissions == null || octalPermissions.length() != 3) {
            throw new IllegalArgumentException("八进制权限必须是3位数字: " + octalPermissions);
        }

        StringBuilder result = new StringBuilder(9);
        char[] octalChars = octalPermissions.toCharArray();

        // 解析每个八进制数字
        for (char c : octalChars) {
            int digit = Character.getNumericValue(c);
            if (digit < 0 || digit > 7) {
                throw new IllegalArgumentException("八进制数字必须在0-7之间: " + c);
            }

            // 转换为 rwx 表示
            result.append((digit & 4) != 0 ? 'r' : '-');
            result.append((digit & 2) != 0 ? 'w' : '-');
            result.append((digit & 1) != 0 ? 'x' : '-');
        }

        return result.toString();
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

    /**
     * 获取文件收藏列表
     *
     * @param fileClient 文件客户端
     * @return 文件收藏列表
     */
    public static List<ShellFileCollect> fileCollect(ShellFileClient<?> fileClient) {
        String iid = fileClient.getShellConnect().getId();
        List<ShellFileCollect> fileCollects = FILE_COLLECT_STORE.loadByIid(iid);
        List<ShellFileCollect> collects = new ArrayList<>();
        for (ShellFileCollect fileCollect : fileCollects) {
            try {
                ShellFile file = fileClient.fileInfo(fileCollect.getContent());
                if (file == null || !file.isDirectory()) {
                    FILE_COLLECT_STORE.delete(fileCollect.getId());
                } else {
                    collects.add(fileCollect);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (ExceptionUtil.hasMessage(ex, "No such file")) {
                    FILE_COLLECT_STORE.delete(fileCollect.getId());
                }
            }
        }
        return collects;
    }

    /**
     * 文件是否被收藏
     *
     * @param fileClient 文件客户端
     * @param file       文件
     * @return 结果
     */
    public static boolean isFileCollect(ShellFileClient<?> fileClient, ShellFile file) {
        String iid = fileClient.getShellConnect().getId();
        return FILE_COLLECT_STORE.exist(iid, file.getFilePath());
    }

    /**
     * 收藏文件
     *
     * @param fileClient 文件客户端
     * @param file       文件
     */
    public static void collectFile(ShellFileClient<?> fileClient, ShellFile file) {
        String iid = fileClient.getShellConnect().getId();
        ShellFileCollect collect = new ShellFileCollect();
        collect.setContent(file.getFilePath());
        collect.setIid(iid);
        FILE_COLLECT_STORE.replace(collect);
    }

    /**
     * 取消收藏文件
     *
     * @param fileClient 文件客户端
     * @param file       文件
     */
    public static void unCollectFile(ShellFileClient<?> fileClient, ShellFile file) {
        String iid = fileClient.getShellConnect().getId();
        FILE_COLLECT_STORE.delete(iid, file.getFilePath());
    }
}
