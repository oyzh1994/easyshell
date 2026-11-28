package cn.oyzh.easyshell.h2.generator.record;

import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.util.h2.ShellH2ColumnUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * H2 记录 SQL 生成器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public final class H2RecordSqlGenerator {

    private H2RecordSqlGenerator() {
    }

    public static String insert(String tableName, Collection<String> columns, Map<String, Object> values) {
        H2GeneratorUtil.requireName(tableName);
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Insert columns are empty");
        }
        List<String> names = new ArrayList<>();
        List<String> data = new ArrayList<>();
        for (String column : columns) {
            names.add(H2GeneratorUtil.quote(column));
            data.add(H2GeneratorUtil.literal(values == null ? null : values.get(column)));
        }
        return "INSERT INTO " + H2GeneratorUtil.quote(tableName) + " (" + String.join(", ", names)
                + ") VALUES (" + String.join(", ", data) + ");";
    }

    /**
     * 按字段类型生成 INSERT，正确处理 JSON、UUID 等 H2 专用字面量。
     */
    public static String insertWithTypes(String tableName, List<H2Column> columns, Map<String, Object> values) {
        return insertWithTypes(null, tableName, columns, values);
    }

    /**
     * 按字段类型生成指定模式中的 INSERT。
     *
     * @param schema    模式名称
     * @param tableName 表名称
     * @param columns   字段
     * @param values    数据
     * @return INSERT 语句
     */
    public static String insertWithTypes(String schema, String tableName,
                                         List<H2Column> columns, Map<String, Object> values) {
        H2GeneratorUtil.requireName(tableName);
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Insert columns are empty");
        }
        List<String> names = new ArrayList<>();
        List<String> data = new ArrayList<>();
        for (H2Column column : columns) {
            names.add(H2GeneratorUtil.quote(column.getName()));
            Object value = values == null ? null : values.get(column.getName());
            data.add(literal(column, value));
        }
        return "INSERT INTO " + H2GeneratorUtil.qualified(schema, tableName)
                + " (" + String.join(", ", names)
                + ") VALUES (" + String.join(", ", data) + ");";
    }

    private static String literal(H2Column column, Object value) {
        if (value == null) {
            return "NULL";
        }
        if (ShellH2ColumnUtil.isJsonType(column.getType())) {
            if (String.valueOf(value).isBlank()) {
                return "NULL";
            }
            return "JSON " + H2GeneratorUtil.literal(String.valueOf(value));
        }
        if ("UUID".equals(ShellH2ColumnUtil.normalizeType(column.getType()))) {
            return "UUID " + H2GeneratorUtil.literal(String.valueOf(value));
        }
        return H2GeneratorUtil.literal(value);
    }
}
