package cn.oyzh.easyshell.sqlite.schema;

/**
 * SQLite schema 对象，用于保存 sqlite_master 中的通用对象信息。
 *
 * @since 2026-11-02
 */
public class SQLiteSchemaObject {

    private String name;

    private String type;

    private String tableName;

    private String sql;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSql() {
        return this.sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
