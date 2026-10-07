package cn.oyzh.easyshell.dameng.index;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 达梦索引
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class DamengIndex extends DBObject implements ObjectCopier<DamengIndex> {

    /**
     * 索引顺序
     */
    private int seqIndex;

    /**
     * 类型
     * 1. normal
     * 2. unique
     */
    private String type;

    /**
     * 方式
     * 1. null|空字符串
     * 2. btree
     * 3. hash
     */
    private String method;

    /**
     * 名称
     */
    private String name;

    /**
     * 字段列表
     */
    private List<IndexColumn> columns;

    /**
     * 获取原始名称
     *
     * @return 原始名称
     */
    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    /**
     * 添加字段
     *
     * @param column 字段名
     */
    public void addColumn(String column) {
        if (this.columns == null) {
            this.setColumns(new ArrayList<>());
        }
        this.columns.add(new IndexColumn(column));
    }

    /**
     * 是否唯一索引
     *
     * @return 结果
     */
    public boolean isUnique() {
        return StringUtil.equalsIgnoreCase(this.getMethod(), "UNIQUE");
    }

    /**
     * 设置字段列表
     *
     * @param columns 字段列表
     */
    public void setColumns(List<IndexColumn> columns) {
        this.columns = columns;
        super.putOriginalData("columns", columns);
    }

    /**
     * 设置类型
     *
     * @param type 类型
     */
    public void setType(String type) {
        this.type = type;
        super.putOriginalData("type", type);
    }

    /**
     * 设置方式
     *
     * @param method 方式
     */
    public void setMethod(String method) {
        this.method = method;
        super.putOriginalData("method", method);
    }

    /**
     * 根据索引类型与唯一性设置类型和方式
     *
     * @param type       索引类型
     * @param noneUnique 非唯一标识
     */
    public void type(String type, int noneUnique) {
        if (StringUtil.equalsIgnoreCase(type, "HASH") && noneUnique == 0) {
            this.setType("UNIQUE");
            this.setMethod("HASH");
        } else if (StringUtil.equalsIgnoreCase(type, "HASH") && noneUnique == 1) {
            this.setType("NORMAL");
            this.setMethod("HASH");
        } else if (StringUtil.equalsIgnoreCase(type, "BTREE") && noneUnique == 0) {
            this.setType("UNIQUE");
            this.setMethod("BTREE");
        } else if (StringUtil.equalsIgnoreCase(type, "BTREE") && noneUnique == 1) {
            this.setType("NORMAL");
            this.setMethod("BTREE");
        } else {
            this.setType("NORMAL");
            this.setMethod("BTREE");
        }
    }

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String typeName() {
        if (this.type == null || "NORMAL".equalsIgnoreCase(this.type)) {
            return null;
        }
        return this.type.toUpperCase();
    }

    /**
     * 获取方式名称
     *
     * @return 方式名称
     */
    public String methodName() {
        return StringUtil.emptyToNull(this.method);
    }

    @Override
    public void copy(DamengIndex t1) {
        if (t1 != null) {
            this.setName(t1.name);
            this.setType(t1.type);
            this.setMethod(t1.method);
            this.setColumns(t1.columns);
            this.setSeqIndex(t1.seqIndex);
        }
    }

    /**
     * 是否无效
     *
     * @return 结果
     */
    public boolean isInvalid() {
        return StringUtil.isBlank(this.name) || StringUtil.isBlank(this.type) || CollectionUtil.isEmpty(this.columns);
    }


    /**
     * 获取索引顺序
     *
     * @return 索引顺序
     */
    public int getSeqIndex() {
        return seqIndex;
    }

    /**
     * 设置索引顺序
     *
     * @param seqIndex 索引顺序
     */
    public void setSeqIndex(int seqIndex) {
        this.seqIndex = seqIndex;
    }

    /**
     * 获取类型
     *
     * @return 类型
     */
    public String getType() {
        return type;
    }

    /**
     * 获取方式
     *
     * @return 方式
     */
    public String getMethod() {
        return method;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public List<IndexColumn> getColumns() {
        return columns;
    }

    /**
     * 索引字段
     */
    public static class IndexColumn {

        /**
         * 字段名
         */
        private String columnName;

        /**
         * 构造索引字段
         */
        public IndexColumn(  ) {
        }

        /**
         * 构造索引字段
         *
         * @param columnName 字段名
         */
        public IndexColumn(String columnName) {
            this.columnName = columnName;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof IndexColumn column) {
                return  StringUtil.equals(this.columnName, column.columnName);
            }
            return false;
        }

        /**
         * 获取字段名
         *
         * @return 字段名
         */
        public String getColumnName() {
            return columnName;
        }

        /**
         * 设置字段名
         *
         * @param columnName 字段名
         */
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
    }
}
