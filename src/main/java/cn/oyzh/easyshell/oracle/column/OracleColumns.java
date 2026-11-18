package cn.oyzh.easyshell.oracle.column;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObjectList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Oracle数据库字段列表
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class OracleColumns extends DBObjectList<OracleColumn> {

    /**
     * 构造Oracle数据库字段列表
     */
    public OracleColumns() {

    }

    /**
     * 构造Oracle数据库字段列表
     *
     * @param list 字段列表
     */
    public OracleColumns(List<OracleColumn> list) {
        super.addAll(list);
    }

    /**
     * 获取主键字段列表
     *
     * @return 主键字段列表
     */
    public List<OracleColumn> primaryKeys() {
        List<OracleColumn> list1 = new ArrayList<>();
        for (OracleColumn column : this) {
            if (column.isPrimaryKey() && !DBObjectList.isDeleted(column)) {
                list1.add(column);
            }
        }
        return list1.parallelStream().filter(OracleColumn::isPrimaryKey).sorted((o1, o2) -> Boolean.compare(o2.isAutoIncrement(), o1.isAutoIncrement())).collect(Collectors.toList());
    }

    /**
     * 主键是否变更
     *
     * @return 变更结果
     */
    public boolean primaryKeyChanged() {
        for (OracleColumn column : this) {
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
    public OracleColumn column(String name) {
        if (!this.isEmpty()) {
            for (OracleColumn dbColumn : this) {
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
        for (OracleColumn dbColumn : this) {
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
    public List<OracleColumn> sortOfPosition() {
        return this.parallelStream().sorted(Comparator.comparing(OracleColumn::getPosition)).collect(Collectors.toList());
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        for (OracleColumn dbColumn : this) {
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
        for (OracleColumn dbColumn : this) {
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
        for (OracleColumn dbColumn : this) {
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
        for (OracleColumn column : this) {
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
        for (OracleColumn column : this) {
            if (column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否IDENTITY自动递增类型
     *
     * @return 结果
     */
    public boolean isIdentityAutoIncrementType() {
        for (OracleColumn column : this) {
            if (column.isIdentityAutoIncrementType()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 默认值是否序列
     *
     * @return 结果
     */
    public boolean isDefaultValueSequence() {
        for (OracleColumn column : this) {
            if (column.isDefaultValueSequence()) {
                return true;
            }
        }
        return false;
    }
}
