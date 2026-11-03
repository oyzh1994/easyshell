package cn.oyzh.easyshell.data.sqlite;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteDataUtil;

import java.util.List;
import java.util.Map;

/**
 * SQLite 数据导入转换工具。
 *
 * @since 2026-11-02
 */
public final class SQLiteDataImportHelper {

    private SQLiteDataImportHelper() {
    }

    public static List<String> toInsertSql(String tableName, List<SQLiteColumn> columns,
                                           List<Map<String, Object>> records) {
        return ShellSqliteDataUtil.toInsertSql(tableName, columns, records);
    }
}
