package cn.oyzh.easyshell.webdav;

import cn.oyzh.common.util.StringUtil;
import com.github.sardine.DavResource;

/**
 *
 * @author oyzh
 * @since 2025-10-09
 */
public class ShellWebdavUtil {

    /**
     * 是否文件自身
     *
     * @param resource 资源
     * @param path     路径
     * @return 结果
     */
    public static boolean isSalf(DavResource resource, String path) {
        if(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String fPath = resource.getPath();
        if(fPath.endsWith("/")) {
            fPath = fPath.substring(0, fPath.length() - 1);
        }
        if (StringUtil.checkCountOccurrences(fPath, '/', 2)) {
            fPath = fPath.substring(fPath.indexOf('/', 1));
            return StringUtil.equals(fPath, path);
        }
        return StringUtil.equals(fPath, path);
    }

    /**
     * 是否根目录
     *
     * @param resource 资源
     * @return 结果
     */
    public static boolean isRoot(DavResource resource) {
        String fPath = resource.getPath();
        if(fPath.endsWith("/")) {
            fPath = fPath.substring(0, fPath.length() - 1);
        }
        return !StringUtil.checkCountOccurrences(fPath, '/', 2);
    }

}
