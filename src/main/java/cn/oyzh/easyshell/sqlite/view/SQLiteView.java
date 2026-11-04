package cn.oyzh.easyshell.sqlite.view;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBView;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 视图。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteView extends DBObject implements DBView, ObjectCopier<SQLiteView>, ObjectComparator<SQLiteView> {

    private String dbName = "main";

    private String name;

    private String comment;

    private String definition;

    private boolean updatable;

    private String createDefinition;

    private SQLiteColumns columns;

    public SQLiteView() {
    }

    public SQLiteView(String name) {
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
        return this.createDefinition;
    }

    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
        super.putOriginalData("createDefinition", createDefinition);
    }

    @Override
    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
        super.putOriginalData("updatable", updatable);
    }

    @Override
    public boolean isUpdatable() {
        return this.updatable;
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

    @Override
    public void copy(SQLiteView view) {
        if (view != null) {
            this.setName(view.getName());
            this.setDbName(view.getDbName());
            this.setComment(view.getComment());
            this.setUpdatable(view.isUpdatable());
            this.setDefinition(view.getDefinition());
            this.setCreateDefinition(view.getCreateDefinition());
            this.setColumns(view.getColumns() == null
                    ? null : new SQLiteColumns(new ArrayList<>(view.getColumns())));
        }
    }

    @Override
    public boolean compare(SQLiteView view) {
        return view != null && java.util.Objects.equals(view.getName(), this.name)
                && java.util.Objects.equals(view.getDefinition(), this.definition)
                && view.isUpdatable() == this.updatable;
    }
}
