package cn.oyzh.easyshell.mongo.util;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/07/26
 */
public class MongoI18nHelper {

    public static String welcome() {
        return I18nResourceBundle.i18nString("mongo.home.welcome");
    }

    public static String tableTip2() {
        return I18nResourceBundle.i18nString("db.table.tip2");
    }

    public static String tableTip3() {
        return I18nResourceBundle.i18nString("db.table.tip3");
    }

    public static String tableTip4() {
        return I18nResourceBundle.i18nString("db.table.tip4");
    }

}
