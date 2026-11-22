package cn.oyzh.easyshell.query.sqlserver;

/**
 * SQL Server查询补全使用的对象索引项。
 *
 * @param schema 模式名称
 * @param name   对象名称
 * @param type   对象类型
 */
public record SqlServerQueryObject(String schema, String name, String type) {
}
