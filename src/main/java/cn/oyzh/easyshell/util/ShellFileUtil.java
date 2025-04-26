package cn.oyzh.easyshell.util;

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
}
