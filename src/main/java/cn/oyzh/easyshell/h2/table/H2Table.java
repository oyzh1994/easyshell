package cn.oyzh.easyshell.h2.table;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.easyshell.h2.check.H2Check;
import cn.oyzh.easyshell.h2.foreignKey.H2ForeignKey;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBTable;

import java.util.ArrayList;
import java.util.List;

/**
 * H2 表。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2Table extends DBObject implements DBTable, ObjectCopier<H2Table>, ObjectComparator<H2Table> {

    private String dbName;

    private String name;

    private String comment;

    private String definition;

    private String type = "table";

    private String constraints;

    private H2Columns columns;

    private List<H2Check> checks;

    private List<H2ForeignKey> foreignKeys;

    public H2Table() {
    }

    public H2Table(String name) {
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

    public String getConstraints() {
        return this.constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
        super.putOriginalData("constraints", constraints);
    }

    public H2Columns getColumns() {
        return this.columns;
    }

    public List<H2Check> getChecks() {
        return this.checks;
    }

    public void setChecks(List<H2Check> checks) {
        this.checks = checks;
        super.putOriginalData("checks", checks);
    }

    public List<H2ForeignKey> getForeignKeys() {
        return this.foreignKeys;
    }

    public void setForeignKeys(List<H2ForeignKey> foreignKeys) {
        this.foreignKeys = foreignKeys;
        super.putOriginalData("foreignKeys", foreignKeys);
    }

    public void setColumns(List<H2Column> columns) {
        this.columns = columns == null ? null : new H2Columns(columns);
        super.putOriginalData("columns", this.columns);
    }

    public void setColumns(H2Columns columns) {
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
            for (H2Column column : this.columns) {
                if (column.isDeleted() || column.isCreated() || column.isColumnChanged()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void copy(H2Table table) {
        if (table != null) {
            this.setName(table.getName());
            this.setDbName(table.getDbName());
            this.setComment(table.getComment());
            this.setDefinition(table.getDefinition());
            this.setType(table.getType());
            this.setConstraints(table.getConstraints());
            this.setColumns(table.getColumns() == null
                    ? null : new H2Columns(new ArrayList<>(table.getColumns())));
            this.setChecks(table.getChecks());
            this.setForeignKeys(table.getForeignKeys());
        }
    }

    @Override
    public boolean compare(H2Table table) {
        return table != null && java.util.Objects.equals(table.getName(), this.name)
                && java.util.Objects.equals(table.getDefinition(), this.definition)
                && java.util.Objects.equals(table.getConstraints(), this.constraints)
                && java.util.Objects.equals(table.getColumns(), this.columns);
    }
}
