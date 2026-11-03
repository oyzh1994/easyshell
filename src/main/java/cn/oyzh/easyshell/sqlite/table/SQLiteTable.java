package cn.oyzh.easyshell.sqlite.table;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBTable;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteTable extends DBObject implements DBTable, ObjectCopier<SQLiteTable>, ObjectComparator<SQLiteTable> {

    private String dbName = "main";

    private String name;

    private String comment;

    private String definition;

    private String type = "table";

    private boolean withoutRowid;

    private boolean strict;

    private String constraints;

    private SQLiteColumns columns;

    public SQLiteTable() {
    }

    public SQLiteTable(String name) {
        this.setName(name);
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public String originalName() {
        return (String) super.getOriginalData("name");
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
        super.putOriginalData("comment", comment);
    }

    public String getDefinition() {
        return this.definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
        super.putOriginalData("definition", definition);
    }

    public String getCreateDefinition() {
        return this.definition;
    }

    public void setCreateDefinition(String createDefinition) {
        this.setDefinition(createDefinition);
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
        super.putOriginalData("type", type);
    }

    public boolean isWithoutRowid() {
        return this.withoutRowid;
    }

    public void setWithoutRowid(boolean withoutRowid) {
        this.withoutRowid = withoutRowid;
        super.putOriginalData("withoutRowid", withoutRowid);
    }

    public boolean isStrict() {
        return this.strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
        super.putOriginalData("strict", strict);
    }

    public String getConstraints() {
        return this.constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
        super.putOriginalData("constraints", constraints);
    }

    public SQLiteColumns getColumns() {
        return this.columns;
    }

    public void setColumns(List<SQLiteColumn> columns) {
        this.columns = columns == null ? null : new SQLiteColumns(columns);
        super.putOriginalData("columns", this.columns);
    }

    public void setColumns(SQLiteColumns columns) {
        this.columns = columns;
        super.putOriginalData("columns", columns);
    }

    public boolean hasPrimaryKey() {
        return this.columns != null && this.columns.hasPrimaryKey();
    }

    public boolean hasAutoIncrement() {
        return this.columns != null && this.columns.hasAutoIncrement();
    }

    public boolean hasComment() {
        return this.comment != null;
    }

    public boolean isColumnChanged() {
        if (this.columns != null) {
            for (SQLiteColumn column : this.columns) {
                if (column.isDeleted() || column.isCreated() || column.isColumnChanged()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void copy(SQLiteTable table) {
        if (table != null) {
            this.setName(table.getName());
            this.setDbName(table.getDbName());
            this.setComment(table.getComment());
            this.setDefinition(table.getDefinition());
            this.setType(table.getType());
            this.setWithoutRowid(table.isWithoutRowid());
            this.setStrict(table.isStrict());
            this.setConstraints(table.getConstraints());
            this.setColumns(table.getColumns() == null
                    ? null : new SQLiteColumns(new ArrayList<>(table.getColumns())));
        }
    }

    @Override
    public boolean compare(SQLiteTable table) {
        return table != null && java.util.Objects.equals(table.getName(), this.name)
                && java.util.Objects.equals(table.getDefinition(), this.definition)
                && java.util.Objects.equals(table.getConstraints(), this.constraints)
                && table.isWithoutRowid() == this.withoutRowid
                && table.isStrict() == this.strict;
    }
}
