package cn.oyzh.easyshell.util.zk;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/5/13
 */

public class ZKI18nHelper {

    public static final String NODE_TIP1 = "zk.nodeTip1";

    public static final String NODE_TIP2 = "zk.nodeTip2";

    public static final String NODE_TIP3 = "zk.nodeTip3";

    public static final String NODE_TIP4 = "zk.nodeTip4";

    public static final String NODE_TIP5 = "zk.nodeTip5";

    public static final String NODE_TIP6 = "zk.nodeTip6";

    public static String nodeTip1() {
        return I18nResourceBundle.i18nString(NODE_TIP1);
    }

    public static String nodeTip2() {
        return I18nResourceBundle.i18nString(NODE_TIP2);
    }

    public static String nodeTip3() {
        return I18nResourceBundle.i18nString(NODE_TIP3);
    }

    public static String nodeTip4() {
        return I18nResourceBundle.i18nString(NODE_TIP4);
    }

    public static String nodeTip5() {
        return I18nResourceBundle.i18nString(NODE_TIP5);
    }

    public static String nodeTip6() {
        return I18nResourceBundle.i18nString(NODE_TIP6);
    }

    public static String aclC() {
        return I18nResourceBundle.i18nString("zk.acl.c");
    }

    public static String migrationTip1() {
        return I18nResourceBundle.i18nString("zk.migration.tip1");
    }

    public static String migrationTip6() {
        return I18nResourceBundle.i18nString("zk.migration.tip6");
    }

    public static String migrationTip8() {
        return I18nResourceBundle.i18nString("zk.migration.tip8");
    }

    public static String nodeTip7() {
        return I18nResourceBundle.i18nString("zk.node.tip7");
    }
}
