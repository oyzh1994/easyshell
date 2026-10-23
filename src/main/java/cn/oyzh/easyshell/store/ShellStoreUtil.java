package cn.oyzh.easyshell.store;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.store.jdbc.JdbcConst;
import cn.oyzh.store.jdbc.JdbcDialect;
import cn.oyzh.store.jdbc.JdbcManager;
import cn.oyzh.store.jdbc.migration.H2SqliteMigrator;

import java.nio.file.Path;

/**
 * shell存储工具类
 *
 * @author oyzh
 * @since 2025-02-14
 */
public class ShellStoreUtil {

    /**
     * 执行初始化
     */
    public static void init() {
        try {
            Path storeDatabase = Path.of(ShellConst.getStorePath(), "db");
            H2SqliteMigrator.migrateIfNeeded(storeDatabase, storeDatabase);
            JdbcConst.dbCacheSize(1024);
            JdbcConst.dbDialect(JdbcDialect.SQLITE);
            JdbcManager.dialect = JdbcDialect.SQLITE;
            JdbcConst.dbFile(storeDatabase.toString());
            JdbcManager.takeoff();
        } catch (Exception ex) {
            if (StringUtil.containsAny(ex.getMessage(), "Database may be already in use")) {
                MessageBox.warn(I18nHelper.programTip1(), StageManager.getFrontWindow());
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
