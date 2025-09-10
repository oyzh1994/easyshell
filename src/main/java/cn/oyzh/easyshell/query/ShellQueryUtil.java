package cn.oyzh.easyshell.query;

import cn.oyzh.common.util.StringUtil;

/**
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellQueryUtil {

    /**
     * 计算相近度
     *
     * @param str  内容
     * @param text 文本
     * @return 结果
     */
    public static double clacCorr(String str, String text) {
        str = str.toUpperCase();
        text = text.toUpperCase();
        if (!str.contains(text) && !text.contains(str)) {
            return 0.d;
        }
        double corr = StringUtil.similarity(str, text);
        if (str.startsWith(text)) {
            corr += 0.3;
        } else if (str.contains(text)) {
            corr += 0.2;
        } else if (str.endsWith(text)) {
            corr += 0.1;
        }
        return corr;
    }

}
