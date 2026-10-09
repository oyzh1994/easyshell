package cn.oyzh.easyshell.mariadb.index;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MariaDB索引
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbIndex extends DBObject implements ObjectCopier<MariadbIndex> {

    /**
     * 索引顺序
     */
    private int seqIndex;

    /**
     * 类型
     * 1. normal
     * 2. unique
     * 3. fulltext
     * 4. spatial
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
     * 注释
     */
    private String comment;

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
     * @param column  字段名
     * @param subPart 子部分
     */
    public void addColumn(String column, int subPart) {
        if (this.columns == null) {
            this.setColumns(new ArrayList<>());
        }
        this.columns.add(new IndexColumn(column, subPart));
    }

    /**
     * 是否唯一索引
     *
     * @return 结果
     */
    public boolean isUnique() {
        return StringUtil.equalsIgnoreCase(this.getType(), "UNIQUE");
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
     * 设置注释
     *
     * @param comment 注释
     */
    public void setComment(String comment) {
        this.comment = comment;
        super.putOriginalData("comment", comment);
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
        } else if (StringUtil.equalsIgnoreCase(type, "fulltext")) {
            this.setType("FULLTEXT");
            this.setMethod("");
        } else if (StringUtil.equalsIgnoreCase(type, "spatial")) {
            this.setType("SPATIAL");
            this.setMethod("");
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
    public void copy(MariadbIndex t1) {
        if (t1 != null) {
            this.setName(t1.name);
            this.setType(t1.type);
            this.setMethod(t1.method);
            this.setComment(t1.comment);
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
     * 获取注释
     *
     * @return 注释
     */
    public String getComment() {
        return comment;
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

    @Override
    public void destroy() {
        if (this.columns != null) {
            this.columns.clear();
            this.columns = null;
        }
        super.destroy();
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
         * 子部分
         */
        private Integer subPart;

        /**
         * 构造索引字段
         */
        public IndexColumn() {
        }

        /**
         * 构造索引字段
         *
         * @param columnName 字段名
         * @param subPart    子部分
         */
        public IndexColumn(String columnName, Integer subPart) {
            this.columnName = columnName;
            this.subPart = subPart;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof IndexColumn column) {
                return Objects.equals(column.subPart, this.subPart) && StringUtil.equals(this.columnName, column.columnName);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.columnName, this.subPart);
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

        /**
         * 获取子部分
         *
         * @return 子部分
         */
        public Integer getSubPart() {
            return subPart;
        }

        /**
         * 设置子部分
         *
         * @param subPart 子部分
         */
        public void setSubPart(Integer subPart) {
            this.subPart = subPart;
        }
    }
}
