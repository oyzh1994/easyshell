package cn.oyzh.easyshell.sqlite.generator.record;

import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * SQLite 记录 SQL 生成器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public final class SQLiteRecordSqlGenerator {

    private SQLiteRecordSqlGenerator() {
    }

    public static String insert(String tableName, Collection<String> columns, Map<String, Object> values) {
        SQLiteGeneratorUtil.requireName(tableName);
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Insert columns are empty");
        }
        List<String> names = new ArrayList<>();
        List<String> data = new ArrayList<>();
        for (String column : columns) {
            names.add(SQLiteGeneratorUtil.quote(column));
            data.add(SQLiteGeneratorUtil.literal(values == null ? null : values.get(column)));
        }
        return "INSERT INTO " + SQLiteGeneratorUtil.quote(tableName) + " (" + String.join(", ", names)
                + ") VALUES (" + String.join(", ", data) + ");";
    }
}
