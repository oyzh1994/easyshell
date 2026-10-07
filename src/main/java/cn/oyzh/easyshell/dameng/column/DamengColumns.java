package cn.oyzh.easyshell.dameng.column;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.fx.db.DBObjectList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 达梦数据库字段列表
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class DamengColumns extends DBObjectList<DamengColumn> {

    /**
     * 构造达梦数据库字段列表
     */
    public DamengColumns() {

    }

    /**
     * 构造达梦数据库字段列表
     *
     * @param list 字段列表
     */
    public DamengColumns(List<DamengColumn> list) {
        super.addAll(list);
    }

    /**
     * 获取主键字段列表
     *
     * @return 主键字段列表
     */
    public List<DamengColumn> primaryKeys() {
        List<DamengColumn> list1 = new ArrayList<>();
        for (DamengColumn column : this) {
            if (column.isPrimaryKey() && !DBObjectList.isDeleted(column)) {
                list1.add(column);
            }
        }
        return list1.parallelStream().filter(DamengColumn::isPrimaryKey).sorted((o1, o2) -> {
            if (o1.isAutoIncrement() && o2.isAutoIncrement()) {
                return 0;
            }
            if (o1.isAutoIncrement() && !o2.isAutoIncrement()) {
                return -1;
            }
            return 1;
        }).collect(Collectors.toList());
    }

    /**
     * 主键是否变更
     *
     * @return 变更结果
     */
    public boolean primaryKeyChanged() {
        for (DamengColumn column : this) {
            if (column.isPrimaryKey() && column.isColumnChanged()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据名称获取字段
     *
     * @param name 字段名称
     * @return 字段
     */
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

    /**
     * 根据名称获取字段位置
     *
     * @param name 字段名称
     * @return 字段位置
     */
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

    /**
     * 按字段位置排序
     *
     * @return 排序后的字段列表
     */
    public List<DamengColumn> sortOfPosition() {
        return this.parallelStream()
                .sorted(Comparator.comparing(DamengColumn::getPosition))
                .collect(Collectors.toList());
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        for (DamengColumn dbColumn : this) {
            return dbColumn.getTableName();
        }
        return null;
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        for (DamengColumn dbColumn : this) {
            return dbColumn.getSchema();
        }
        return null;
    }

    /**
     * 获取字段名称列表
     *
     * @return 字段名称列表
     */
    public List<String> columnNames() {
        List<String> list = new ArrayList<>();
        for (DamengColumn dbColumn : this) {
            list.add(dbColumn.getName());
        }
        return list;
    }

    /**
     * 是否存在主键
     *
     * @return 是否存在主键
     */
    public boolean hasPrimaryKey() {
        for (DamengColumn column : this) {
            if (column.isPrimaryKey() || column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否存在自动递增字段
     *
     * @return 是否存在自动递增字段
     */
    public boolean hasAutoIncrement() {
        for (DamengColumn column : this) {
            if (column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }
}
