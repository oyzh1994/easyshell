package cn.oyzh.easyshell.util.zk;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/5/13
 */

public class ZKI18nHelper {

    public static final String NODE_TIP1 = "shell.zk.nodeTip1";

    public static final String NODE_TIP2 = "shell.zk.nodeTip2";

    public static final String NODE_TIP3 = "shell.zk.nodeTip3";

    public static final String NODE_TIP4 = "shell.zk.nodeTip4";

    public static final String NODE_TIP5 = "shell.zk.nodeTip5";

    public static final String NODE_TIP6 = "shell.zk.nodeTip6";

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
        return I18nResourceBundle.i18nString("shell.zk.acl.c");
    }

    public static String migrationTip1() {
        return I18nResourceBundle.i18nString("shell.zk.migration.tip1");
    }

    public static String migrationTip6() {
        return I18nResourceBundle.i18nString("shell.zk.migration.tip6");
    }

    public static String migrationTip8() {
        return I18nResourceBundle.i18nString("shell.zk.migration.tip8");
    }

    public static String nodeTip7() {
        return I18nResourceBundle.i18nString("shell.zk.node.tip7");
    }
}
