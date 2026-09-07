package cn.oyzh.easyshell.mysql.table;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBTable;
import javafx.beans.property.SimpleStringProperty;

/**
 * db表
 *
 * @author oyzh
 * @since 2024/01/16
 */
public class MysqlTable extends DBObject implements DBTable, ObjectCopier<MysqlTable>, ObjectComparator<MysqlTable> {

    /**
     * 是否有主键
     */
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

    /**
     * 库名称
     */
    private String dbName;

    /**
     * 模式名称
     */
    private String schema;

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

    @Override
    public void setName(String name) {
        this.nameProperty().setValue(name);
    }

    @Override
    public String getName() {
        return this.nameProperty == null ? null : this.nameProperty.get();
    }

    public SimpleStringProperty commentProperty() {
        if (this.commentProperty == null) {
            this.commentProperty = new SimpleStringProperty();
        }
        return this.commentProperty;
    }

    @Override
    public void setComment(String comment) {
        this.commentProperty().setValue(comment);
    }

    @Override
    public String getComment() {
        return this.commentProperty == null ? null : this.commentProperty.get();
    }

    @Override
    public boolean hasComment() {
        return this.getComment() != null;
    }

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

//    /**
//     * 是否新数据
//     *
//     * @return 结果
//     */
//
//    public boolean isNew() {
//        return StringUtil.isBlank(this.getName());
//    }

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

    @Override
    public void destroy() {
        if (this.nameProperty != null) {
            this.nameProperty.unbind();
            this.nameProperty = null;
        }
        if (this.commentProperty != null) {
            this.commentProperty.unbind();
            this.commentProperty = null;
        }
        super.destroy();
    }
}




