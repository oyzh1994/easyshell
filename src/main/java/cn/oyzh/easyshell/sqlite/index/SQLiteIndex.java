package cn.oyzh.easyshell.sqlite.index;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaManager;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaObject;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * SQLite 索引。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteIndex extends DBObject implements ObjectCopier<SQLiteIndex> {

    private String name;

    private String originalName;

    private String definition;

    private String originalDefinition;

    private String tableName;

    private String createDefinition;

    public SQLiteIndex() {
    }

    public SQLiteIndex(String name) {
        this.setName(name);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public String originalName() {
        return this.originalName == null
                ? (String) super.getOriginalData("name") : this.originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getDefinition() {
        return this.definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
        super.putOriginalData("definition", definition);
    }

    public String getOriginalDefinition() {
        return this.originalDefinition;
    }

    public void setOriginalDefinition(String originalDefinition) {
        this.originalDefinition = originalDefinition;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        super.putOriginalData("tableName", tableName);
    }

    public String getCreateDefinition() {
        return this.createDefinition;
    }

    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }

    public boolean isChangedDefinition() {
        return !StringUtil.equals(this.originalDefinition, this.definition)
                || !StringUtil.equals(this.originalName(), this.name);
    }

    public boolean isInvalid() {
        return StringUtil.isBlank(this.name) || StringUtil.isBlank(this.definition);
    }

    public String normalizedDefinition(String previousTable, String targetTable) {
        String sql = SQLiteSchemaManager.replaceCreateObjectName(this.definition.trim(), this.name);
        String oldTable = StringUtil.blankToDefault(this.tableName, previousTable);
        if (StringUtil.isNotBlank(oldTable) && StringUtil.isNotBlank(targetTable)) {
            sql = SQLiteSchemaManager.replaceTableReference(sql, oldTable, targetTable);
        }
        return sql;
    }

    @Override
    public void copy(SQLiteIndex index) {
        if (index != null) {
            this.setName(index.getName());
            this.setOriginalName(index.originalName());
            this.setDefinition(index.getDefinition());
            this.setOriginalDefinition(index.getOriginalDefinition());
            this.setTableName(index.getTableName());
            this.setCreateDefinition(index.getCreateDefinition());
        }
    }

    public static SQLiteIndex of(SQLiteSchemaObject object) {
        SQLiteIndex index = new SQLiteIndex();
        index.setName(object.getName());
        index.setOriginalName(object.getName());
        index.setDefinition(object.getSql());
        index.setOriginalDefinition(object.getSql());
        index.setTableName(object.getTableName());
        return index;
    }

    public static SQLiteIndex newIndex(String tableName, String columnName) {
        SQLiteIndex index = new SQLiteIndex();
        index.setName(DBUtil.genIndexName());
        index.setDefinition("CREATE INDEX " + SQLiteGeneratorUtil.quote(index.getName())
                + " ON " + SQLiteGeneratorUtil.quote(tableName) + " ("
                + (StringUtil.isBlank(columnName) ? "" : SQLiteGeneratorUtil.quote(columnName)) + ");");
        index.setTableName(tableName);
        index.setCreated(true);
        return index;
    }
}
