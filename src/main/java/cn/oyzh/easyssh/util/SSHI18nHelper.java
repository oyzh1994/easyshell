package cn.oyzh.easyssh.util;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import lombok.experimental.UtilityClass;

/**
 * @author oyzh
 * @since 2025/03/06
 */
@UtilityClass
public class SSHI18nHelper {

    public static String fileTip1() {
        return I18nResourceBundle.i18nString("ssh.file.tip1");
    }

    public static String fileTip2() {
        return I18nResourceBundle.i18nString("ssh.file.tip2");
    }

    public static String fileTip3() {
        return I18nResourceBundle.i18nString("ssh.file.tip3");
    }

}
