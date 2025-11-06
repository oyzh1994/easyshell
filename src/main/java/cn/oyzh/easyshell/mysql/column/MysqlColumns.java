package cn.oyzh.easyshell.mysql.column;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.DBObjectList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * db外键列表
 *
 * @author oyzh
 * @since 2024/07/10
 */
public class MysqlColumns extends DBObjectList<MysqlColumn> {

    public MysqlColumns() {

    }

    public MysqlColumns(List<MysqlColumn> list) {
        super.addAll(list);
    }

    public List<MysqlColumn> primaryKeys() {
        List<MysqlColumn> list1 = new ArrayList<>();
        for (MysqlColumn column : this) {
            if (column.isPrimaryKey() && !DBObjectList.isDeleted(column)) {
                list1.add(column);
            }
        }
        return list1.parallelStream().filter(MysqlColumn::isPrimaryKey).sorted((o1, o2) -> {
            if (o1.isAutoIncrement() && !o2.isAutoIncrement()) {
                return -1;
            }
            if (o1.isAutoIncrement() && o2.isAutoIncrement()) {
                return 0;
            }
            return 1;
        }).collect(Collectors.toList());
    }

    public boolean primaryKeyChanged() {
        return false;
    }

    public MysqlColumn column(String name) {
        if (!this.isEmpty()) {
            for (MysqlColumn dbColumn : this) {
                if (StringUtil.equalsAnyIgnoreCase(dbColumn.getName(), name)) {
                    return dbColumn;
                }
            }
        }
        return null;
    }

    public int index(String name) {
        int index = 0;
        for (MysqlColumn dbColumn : this) {
            if (dbColumn.getName().equals(name)) {
                break;
            }
            index++;
        }
        return index;
    }

    public List<MysqlColumn> sortOfPosition() {
        return this.parallelStream()
                .sorted(Comparator.comparing(MysqlColumn::getPosition))
                .collect(Collectors.toList());
    }

    public String tableName() {
        for (MysqlColumn dbColumn : this) {
            return dbColumn.getTableName();
        }
        return null;
    }

    public String dbName() {
        for (MysqlColumn dbColumn : this) {
            return dbColumn.getDbName();
        }
        return null;
    }

    public List<String> columnNames() {
        List<String> list = new ArrayList<>();
        for (MysqlColumn dbColumn : this) {
            list.add(dbColumn.getName());
        }
        return list;
    }

    public boolean hasPrimaryKey() {
        for (MysqlColumn column : this) {
            if (column.isPrimaryKey() || column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }
}
