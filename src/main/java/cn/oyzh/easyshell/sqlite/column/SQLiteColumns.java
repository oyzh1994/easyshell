package cn.oyzh.easyshell.sqlite.column;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObjectList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SQLite 字段列表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteColumns extends DBObjectList<SQLiteColumn> {

    public SQLiteColumns() {
    }

    public SQLiteColumns(List<SQLiteColumn> columns) {
        super.addAll(columns);
    }

    public List<SQLiteColumn> primaryKeys() {
        List<SQLiteColumn> columns = new ArrayList<>();
        for (SQLiteColumn column : this) {
            if (column.isPrimaryKey() && !DBObjectList.isDeleted(column)) {
                columns.add(column);
            }
        }
        return columns.parallelStream().filter(SQLiteColumn::isPrimaryKey)
                .sorted((left, right) -> Boolean.compare(right.isAutoIncrement(), left.isAutoIncrement()))
                .collect(Collectors.toList());
    }

    public boolean primaryKeyChanged() {
        for (SQLiteColumn column : this) {
            if (column.isPrimaryKey() && column.isColumnChanged()) {
                return true;
            }
        }
        return false;
    }

    public SQLiteColumn column(String name) {
        if (!this.isEmpty()) {
            for (SQLiteColumn column : this) {
                if (StringUtil.equalsAnyIgnoreCase(column.getName(), name)) {
                    return column;
                }
            }
        }
        return null;
    }

    public int index(String name) {
        for (int index = 0; index < this.size(); index++) {
            if (StringUtil.equals(this.get(index).getName(), name)) {
                return index;
            }
        }
        return -1;
    }

    public List<SQLiteColumn> sortOfPosition() {
        return this.parallelStream()
                .sorted(Comparator.comparing(SQLiteColumn::getPosition,
                        Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    public String tableName() {
        for (SQLiteColumn column : this) {
            return column.getTableName();
        }
        return null;
    }

    public String dbName() {
        for (SQLiteColumn column : this) {
            return column.getDbName();
        }
        return null;
    }

    public List<String> columnNames() {
        List<String> names = new ArrayList<>();
        for (SQLiteColumn column : this) {
            names.add(column.getName());
        }
        return names;
    }

    public boolean hasPrimaryKey() {
        for (SQLiteColumn column : this) {
            if (column.isPrimaryKey() || column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAutoIncrement() {
        for (SQLiteColumn column : this) {
            if (column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }
}
