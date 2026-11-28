package cn.oyzh.easyshell.util.h2;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.query.h2.H2ExecuteResult;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.easyshell.h2.record.H2RecordPrimaryKey;
import cn.oyzh.easyshell.h2.ShellH2ConnConfig;
import cn.oyzh.easyshell.h2.ShellH2Helper;
import cn.oyzh.fx.db.query.DBQueryResults;

import java.util.Properties;

/**
 * H2 通用工具入口。
 *
 * @since 2026-11-02
 */
public final class ShellH2Util {

    private ShellH2Util() {
    }

    public static ShellH2ConnConfig connConfig(ShellConnect connect) {
        return ShellH2Helper.connConfig(connect);
    }

    public static String defaultEnvironment() {
        return ShellH2Helper.defaultEnvironment();
    }

    public static boolean environmentBoolean(String env, String key, boolean defaultValue) {
        return ShellH2Helper.environmentBoolean(env, key, defaultValue);
    }

    public static String setEnvironmentValue(String env, String key, String value) {
        return ShellH2Helper.setEnvironmentValue(env, key, value);
    }

    public static Properties parseEnvironment(String env) {
        return ShellH2Helper.parseEnvironment(env);
    }

    public static boolean isTransactionControl(String sql) {
        return ShellH2Helper.isTransactionControl(sql);
    }

    public static String transactionCommand(String sql) {
        return ShellH2Helper.transactionCommand(sql);
    }

    public static String quote(String name) {
        return ShellH2Helper.quote(name);
    }

    public static String displayValue(Object value) {
        return ShellH2Helper.displayValue(value);
    }

    public static String formatResult(H2ExecuteResult result) {
        return ShellH2Helper.formatResult(result);
    }

    public static String formatResults(DBQueryResults<H2ExecuteResult> results) {
        return ShellH2Helper.formatResults(results);
    }

    /**
     * 初始化主键
     *
     * @param columns 字段列表
     * @param record  记录
     * @return 主键
     */
    public static H2RecordPrimaryKey initPrimaryKey(H2Columns columns, H2Record record) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        H2Column column = CollectionUtil.getFirst(columns.primaryKeys());
        if (column == null) {
            return null;
        }
        H2RecordPrimaryKey primaryKey = new H2RecordPrimaryKey();
        primaryKey.init(column, record);
        return primaryKey;
    }
}
