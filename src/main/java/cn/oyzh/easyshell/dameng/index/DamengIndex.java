package cn.oyzh.easyshell.dameng.index;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObject;

import java.util.ArrayList;
import java.util.List;

/**
 * db表索引
 *
 * @author oyzh
 * @since 2024/01/24
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

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public void addColumn(String column) {
        if (this.columns == null) {
            this.setColumns(new ArrayList<>());
        }
        this.columns.add(new IndexColumn(column));
    }

    public boolean isUnique() {
        return StringUtil.equalsIgnoreCase(this.getMethod(), "UNIQUE");
    }

    public void setColumns(List<IndexColumn> columns) {
        this.columns = columns;
        super.putOriginalData("columns", columns);
    }

    public void setType(String type) {
        this.type = type;
        super.putOriginalData("type", type);
    }

    public void setMethod(String method) {
        this.method = method;
        super.putOriginalData("method", method);
    }

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

    public String typeName() {
        if (this.type == null || "NORMAL".equalsIgnoreCase(this.type)) {
            return null;
        }
        return this.type.toUpperCase();
    }

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

    public boolean isInvalid() {
        return StringUtil.isBlank(this.name) || StringUtil.isBlank(this.type) || CollectionUtil.isEmpty(this.columns);
    }


    public int getSeqIndex() {
        return seqIndex;
    }

    public void setSeqIndex(int seqIndex) {
        this.seqIndex = seqIndex;
    }

    public String getType() {
        return type;
    }

    public String getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

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

        public IndexColumn(  ) {
        }

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

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }
    }
}
