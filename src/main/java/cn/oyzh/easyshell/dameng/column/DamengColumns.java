package cn.oyzh.easyshell.dameng.column;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.fx.db.DBObjectList;

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
public class DamengColumns extends DBObjectList<DamengColumn> {

    public DamengColumns() {

    }

    public DamengColumns(List<DamengColumn> list) {
        super.addAll(list);
    }

    public List<DamengColumn> primaryKeys() {
        List<DamengColumn> list1 = new ArrayList<>();
        for (DamengColumn column : this) {
            if (column.isPrimaryKey() && !DBObjectList.isDeleted(column)) {
                list1.add(column);
            }
        }
        return list1.parallelStream().filter(DamengColumn::isPrimaryKey).sorted((o1, o2) -> {
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

    public DamengColumn column(String name) {
        if (!this.isEmpty()) {
            for (DamengColumn dbColumn : this) {
                if (StringUtil.equalsAnyIgnoreCase(dbColumn.getName(), name)) {
                    return dbColumn;
                }
            }
        }
        return null;
    }

    public int index(String name) {
        int index = 0;
        for (DamengColumn dbColumn : this) {
            if (dbColumn.getName().equals(name)) {
                break;
            }
            index++;
        }
        return index;
    }

    public List<DamengColumn> sortOfPosition() {
        return this.parallelStream()
                .sorted(Comparator.comparing(DamengColumn::getPosition))
                .collect(Collectors.toList());
    }

    public String tableName() {
        for (DamengColumn dbColumn : this) {
            return dbColumn.getTableName();
        }
        return null;
    }

    public String schema() {
        for (DamengColumn dbColumn : this) {
            return dbColumn.getSchema();
        }
        return null;
    }

    public List<String> columnNames() {
        List<String> list = new ArrayList<>();
        for (DamengColumn dbColumn : this) {
            list.add(dbColumn.getName());
        }
        return list;
    }

    public boolean hasPrimaryKey() {
        for (DamengColumn column : this) {
            if (column.isPrimaryKey() || column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAutoIncrement() {
        for (DamengColumn column : this) {
            if (column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }
}
