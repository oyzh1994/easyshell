package cn.oyzh.easyshell.h2.column;

/**
 * H2 查询字段参数。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2SelectColumnParam {

    /**
     * 库名称
     */
    private String dbName = "main";

    /**
     * 表或视图名称
     */
    private String tableName;

    /**
     * 对象名称，兼容表/视图查询入口
     */
    private String objectName;

    public H2SelectColumnParam() {
    }

    public H2SelectColumnParam(String objectName) {
        this.objectName = objectName;
        this.tableName = objectName;
    }

    public H2SelectColumnParam(String dbName, String tableName) {
        this.dbName = dbName;
        this.tableName = tableName;
        this.objectName = tableName;
    }

    public String getDbName() {
        return this.dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        this.objectName = tableName;
    }

    public String getObjectName() {
        return this.objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
        this.tableName = objectName;
    }
}
