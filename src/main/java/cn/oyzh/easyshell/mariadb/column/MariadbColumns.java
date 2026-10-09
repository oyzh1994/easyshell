package cn.oyzh.easyshell.mariadb.column;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObjectList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MariaDB字段列表
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbColumns extends DBObjectList<MariadbColumn> {

    /**
     * 构造字段列表
     */
    public MariadbColumns() {

    }

    /**
     * 构造字段列表
     *
     * @param list 字段列表
     */
    public MariadbColumns(List<MariadbColumn> list) {
        super.addAll(list);
    }

    /**
     * 获取主键字段列表
     *
     * @return 主键字段列表
     */
    public List<MariadbColumn> primaryKeys() {
        List<MariadbColumn> list1 = new ArrayList<>();
        for (MariadbColumn column : this) {
            if (column.isPrimaryKey() && !DBObjectList.isDeleted(column)) {
                list1.add(column);
            }
        }
        return list1.parallelStream().filter(MariadbColumn::isPrimaryKey)
                .sorted((o1, o2) -> Boolean.compare(o2.isAutoIncrement(), o1.isAutoIncrement()))
                .collect(Collectors.toList());
    }

    /**
     * 主键是否变更
     *
     * @return 结果
     */
    public boolean primaryKeyChanged() {
        for (MariadbColumn column : this) {
            if (column.isPrimaryKey() && column.isColumnChanged()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据名称获取字段
     *
     * @param name 名称
     * @return 字段
     */
    public MariadbColumn column(String name) {
        if (!this.isEmpty()) {
            for (MariadbColumn dbColumn : this) {
                if (StringUtil.equalsAnyIgnoreCase(dbColumn.getName(), name)) {
                    return dbColumn;
                }
            }
        }
        return null;
    }

    /**
     * 获取字段位置
     *
     * @param name 名称
     * @return 字段位置
     */
    public int index(String name) {
        for (int i = 0; i < this.size(); i++) {
            if (StringUtil.equals(this.get(i).getName(), name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 按字段位置排序
     *
     * @return 排序后的字段列表
     */
    public List<MariadbColumn> sortOfPosition() {
        return this.parallelStream()
                .sorted(Comparator.comparing(MariadbColumn::getPosition))
                .collect(Collectors.toList());
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        for (MariadbColumn dbColumn : this) {
            return dbColumn.getTableName();
        }
        return null;
    }

    /**
     * 获取库名称
     *
     * @return 库名称
     */
    public String dbName() {
        for (MariadbColumn dbColumn : this) {
            return dbColumn.getDbName();
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
        for (MariadbColumn dbColumn : this) {
            list.add(dbColumn.getName());
        }
        return list;
    }

    /**
     * 是否包含主键
     *
     * @return 结果
     */
    public boolean hasPrimaryKey() {
        for (MariadbColumn column : this) {
            if (column.isPrimaryKey() || column.isAutoIncrement()) {
                return true;
            }
        }
        return false;
    }
}
