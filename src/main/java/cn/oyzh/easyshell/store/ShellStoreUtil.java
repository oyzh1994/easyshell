package cn.oyzh.easyshell.store;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.store.jdbc.JdbcConst;
import cn.oyzh.store.jdbc.JdbcDialect;
import cn.oyzh.store.jdbc.JdbcManager;

/**
 * @author oyzh
 * @since 2025-02-14
 */
public class ShellStoreUtil {

    /**
     * 执行初始化
     */
    public static void init() {
        JdbcConst.dbCacheSize(1024);
        JdbcConst.dbDialect(JdbcDialect.H2);
        JdbcConst.dbFile(ShellConst.getStorePath() + "db");
        try {
            JdbcManager.takeoff();
        } catch (Exception ex) {
            if (StringUtil.containsAny(ex.getMessage(), "Database may be already in use")) {
                MessageBox.warn(I18nHelper.programTip1());
            }
        }
    }

    /**
     * 销毁
     */
    public static void destroy() {
        JdbcManager.destroy();
    }
}
