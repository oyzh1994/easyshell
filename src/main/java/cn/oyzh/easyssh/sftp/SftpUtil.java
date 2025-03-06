package cn.oyzh.easyssh.sftp;

import lombok.experimental.UtilityClass;

/**
 * @author oyzh
 * @since 2025-03-06
 */
@UtilityClass
public class SftpUtil {

    public static String concat(String src, String name) {
        if (src.endsWith("/") && name.startsWith("/")) {
            return src + name.substring(1);
        }
        if (!src.endsWith("/") && !name.startsWith("/")) {
            return src + "/" + name;
        }
        return src + name;
    }
}
