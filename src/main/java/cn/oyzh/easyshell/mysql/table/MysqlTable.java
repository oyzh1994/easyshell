package cn.oyzh.easyshell.mysql.table;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mysql.DBObjectStatus;
import javafx.beans.property.SimpleStringProperty;

/**
 * db表
 *
 * @author oyzh
 * @since 2024/01/16
 */
public class MysqlTable extends DBObjectStatus implements ObjectCopier<MysqlTable>, ObjectComparator<MysqlTable> {

    private boolean hasPrimaryKey;

    /**
     * 行格式
     */
    private String rowFormat;

    /**
     * 自动递增值
     */
    private Long autoIncrement;

    /**
     * 表创建定义
     */
    private String createDefinition;

    // /**
    //  * 索引
    //  */
    // @Getter
    // @Setter
    // private MysqlIndexes indexes;

    // /**
    //  * 触发器
    //  */
    // @Getter
    // @Setter
    // private MysqlTriggers triggers;

    // /**
    //  * 外键
    //  */
    // @Getter
    // @Setter
    // private MysqlForeignKeys foreignKeys;

    /**
     * 引擎
     */
    private String engine;

    /**
     * 字符集
     */
    private String charset;

    /**
     * 排序规则
     */
    private String collation;

    // /**
    //  * 检查器
    //  */
    // @Getter
    // @Setter
    // private MysqlChecks checks;

    public void setEngine(String engine) {
        this.engine = engine;
        super.putOriginalData("engine", engine);
    }

    public boolean isEngineChanged() {
        return super.checkOriginalData("engine", this.engine);
    }

    public void setCharset(String charset) {
        this.charset = charset;
        super.putOriginalData("charset", charset);
    }

    public boolean isCharsetChanged() {
        return super.checkOriginalData("charset", this.charset);
    }

    public void setCollation(String collation) {
        this.collation = collation;
        super.putOriginalData("collation", collation);
    }

    public boolean isCollationChanged() {
        return super.checkOriginalData("collation", this.collation);
    }

    public void setRowFormat(String rowFormat) {
        this.rowFormat = rowFormat;
        super.putOriginalData("rowFormat", rowFormat);
        // this.updateChanged();
    }

    public boolean isRowFormatChanged() {
        return super.checkOriginalData("rowFormat", this.rowFormat);
    }

    public void setAutoIncrement(Long autoIncrement) {
        this.autoIncrement = autoIncrement;
        super.putOriginalData("autoIncrement", autoIncrement);
    }

    public boolean isAutoIncrementChanged() {
        return super.checkOriginalData("autoIncrement", this.autoIncrement);
    }

    // public boolean hasIndex() {
    //     return this.indexes != null && !this.indexes.isEmpty();
    // }
    //
    // public boolean hasForeignKey() {
    //     return CollUtil.isNotEmpty(this.foreignKeys);
    // }

    // public boolean hasCheck() {
    //     return CollUtil.isNotEmpty(this.checks);
    // }

    public boolean hasCharset() {
        return StringUtil.isNotBlank(this.charset);
    }

    public boolean hasCollation() {
        return StringUtil.isNotBlank(this.collation);
    }

    public boolean hasEngine() {
        return this.getEngine() != null;
    }

    public void setCharsetAndCollation(String collation) {
        if (StringUtil.isNotBlank(collation)) {
            String charset = collation.split("_")[0];
            this.setCharset(charset);
            this.setCollation(collation);
        }
    }

    // public boolean hasTrigger() {
    //     return this.triggers != null && !this.triggers.isEmpty();
    // }
    //
    // public MysqlIndexes indexes() {
    //     if (this.indexes == null) {
    //         this.indexes = new MysqlIndexes();
    //     }
    //     return this.indexes;
    // }
    //
    // public MysqlTriggers triggers() {
    //     if (this.triggers == null) {
    //         this.triggers = new MysqlTriggers();
    //     }
    //     return this.triggers;
    // }
    //
    // public MysqlForeignKeys foreignKeys() {
    //     if (this.foreignKeys == null) {
    //         this.foreignKeys = new MysqlForeignKeys();
    //     }
    //     return this.foreignKeys;
    // }

    // public MysqlChecks checks() {
    //     if (this.checks == null) {
    //         this.checks = new MysqlChecks();
    //     }
    //     return this.checks;
    // }


    public boolean hasAutoIncrement() {
        return this.getAutoIncrement() != null;
    }

    @Override
    public void copy(MysqlTable table) {
        if (table != null) {
            this.setEngine(table.getEngine());
            this.setComment(table.getComment());
            this.setCharset(table.getCharset());
            this.setRowFormat(table.getRowFormat());
            this.setCollation(table.getCollation());
            this.setHasPrimaryKey(table.isHasPrimaryKey());
            this.setAutoIncrement(table.getAutoIncrement());
            this.setCreateDefinition(table.getCreateDefinition());
        }
    }

    public boolean isInnoDB() {
        return "innodb".equalsIgnoreCase(this.getEngine());
    }

    public boolean hasRowFormat() {
        return StringUtil.isNotBlank(this.getRowFormat());
    }

    // public void removeIndex(MysqlIndex index) {
    //     if (index != null && this.indexes != null) {
    //         this.indexes().remove(index);
    //     }
    // }
    //
    // public void removeTrigger(MysqlTrigger trigger) {
    //     if (trigger != null && this.triggers != null) {
    //         this.triggers().remove(trigger);
    //     }
    // }
    //
    // public void removeForeignKey(MysqlForeignKey foreignKey) {
    //     if (foreignKey != null && this.foreignKeys != null) {
    //         this.foreignKeys().remove(foreignKey);
    //     }
    // }

    // public void removeCheck(MysqlCheck check) {
    //     if (check != null && this.checks != null) {
    //         this.checks().remove(check);
    //     }
    // }

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 模式名称
     */
    private String schema;

    // /**
    //  * 表字段
    //  */
    // @Setter
    // @Getter
    // protected MysqlColumns columns;

    /**
     * 表名称
     */
    private SimpleStringProperty nameProperty;

    /**
     * 表注释
     */
    private SimpleStringProperty commentProperty;

    public SimpleStringProperty nameProperty() {
        if (this.nameProperty == null) {
            this.nameProperty = new SimpleStringProperty();
        }
        return this.nameProperty;
    }

    public void setName(String name) {
        this.nameProperty().setValue(name);
    }

    public String getName() {
        return this.nameProperty == null ? null : this.nameProperty.get();
    }

    public SimpleStringProperty commentProperty() {
        if (this.commentProperty == null) {
            this.commentProperty = new SimpleStringProperty();
        }
        return this.commentProperty;
    }

    public void setComment(String comment) {
        this.commentProperty().setValue(comment);
    }

    public String getComment() {
        return this.commentProperty == null ? null : this.commentProperty.get();
    }

    // public boolean primaryKeyChanged() {
    //     if (this.hasColumns()) {
    //         boolean b1 = this.columns.primaryKeyChanged();
    //         if (b1) {
    //             return true;
    //         }
    //         for (MysqlColumn column : this.columns.createdList()) {
    //             if (column.isPrimaryKey()) {
    //                 return true;
    //             }
    //         }
    //     }
    //     return false;
    // }
    //
    // public List<MysqlColumn> primaryKeys() {
    //     if (this.hasColumns()) {
    //         return this.columns.primaryKeys();
    //     }
    //     return Collections.emptyList();
    // }

    // public boolean hasPrimaryKey() {
    //     return CollUtil.isNotEmpty(this.primaryKeys());
    // }

    // public boolean hasColumns() {
    //     return this.columns != null && !this.columns.isEmpty();
    // }

    public boolean hasComment() {
        return this.getComment() != null;
    }

    // public MysqlColumns columns() {
    //     if (this.columns == null) {
    //         this.columns = new MysqlColumns();
    //     }
    //     return this.columns;
    // }

    @Override
    public boolean compare(MysqlTable table) {
        if (table == null) {
            return false;
        }
        if (table == this) {
            return true;
        }
        if (!StringUtil.equals(this.getName(), table.getName())) {
            return false;
        }
        return StringUtil.equals(this.getDbName(), table.getDbName());
    }

    // public void removeColumn(MysqlColumn column) {
    //     if (column != null && this.columns != null) {
    //         this.columns().remove(column);
    //     }
    // }

    /**
     * 是否新数据
     *
     * @return 结果
     */

    public boolean isNew() {
        return StringUtil.isBlank(this.getName());
    }

    public boolean isHasPrimaryKey() {
        return hasPrimaryKey;
    }

    public void setHasPrimaryKey(boolean hasPrimaryKey) {
        this.hasPrimaryKey = hasPrimaryKey;
    }

    public String getRowFormat() {
        return rowFormat;
    }

    public Long getAutoIncrement() {
        return autoIncrement;
    }

    public String getCreateDefinition() {
        return createDefinition;
    }

    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }

    public String getEngine() {
        return engine;
    }

    public String getCharset() {
        return charset;
    }

    public String getCollation() {
        return collation;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }


}




