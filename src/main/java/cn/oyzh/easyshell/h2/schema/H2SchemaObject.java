package cn.oyzh.easyshell.h2.schema;

/**
 * H2 schema 对象，用于保存 INFORMATION_SCHEMA 中的通用对象信息（表/视图/索引/触发器）。
 *
 * @since 2026-11-28
 */
public class H2SchemaObject {

    private String name;

    private String type;

    private String tableName;

    private String sql;

    private String remark;

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

    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
