package cn.oyzh.easyshell.mongo.util;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.ShellConst;

/**
 * 文件工具类
 *
 * @author oyzh
 * @since 2025-04-28
 */
public class ShellFileUtil {

    /**
     * 文件是否可查看
     *
     * @param extName 扩展名
     * @return 结果
     * null 不可查看
     * txt 文本类型
     * img 图片类型
     * video 视频类型
     * audio 音频类型
     */
    public static String fileViewable(String extName) {
        String type = null;
        if (StringUtil.equalsAnyIgnoreCase(extName,
                "png", "jpg", "jpeg", "gif", "ico", "bmp", "wbmp", "tiff", "webp"
        )) {
            type = "img";
        } else if (StringUtil.equalsAnyIgnoreCase(extName,
                "mp4", "flv", "webm", "mkv", "mov", "wmv", "avi", "3gp", "m4v"
        )) {
            type = "video";
        } else if (StringUtil.equalsAnyIgnoreCase(extName,
                "mp3", "wav", "aac", "aiff", "flac", "pcm", "m4a", "ogg", "wma"
        )) {
            type = "audio";
        } else if (StringUtil.equalsAnyIgnoreCase(extName,
                "txt", "text", "log", "yaml", "java",
                "xml", "json", "htm", "html", "xhtml",
                "php", "css", "c", "cpp", "rs",
                "js", "csv", "sql", "md", "ini",
                "cfg", "sh", "bat", "py", "asp",
                "aspx", "env", "tsv", "conf",
                "plist"
        )) {
            type = "txt";
        } else {
            type = "unknown";
        }
        return type;
    }

    /**
     * 获取临时文件
     *
     * @param extName 文件扩展名
     * @return 临时文件目录
     */
    public static String getTempFile(String extName) {
        // 目标路径
        return ShellConst.getCachePath() + UUIDUtil.uuidSimple() + "." + extName;
    }
}
