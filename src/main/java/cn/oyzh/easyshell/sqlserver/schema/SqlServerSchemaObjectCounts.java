package cn.oyzh.easyshell.sqlserver.schema;

/**
 * SQL Server模式对象数量。
 *
 * @param tables     表数量
 * @param views      视图数量
 * @param functions  函数数量
 * @param procedures 过程数量
 * @param sequences  序列数量
 */
public record SqlServerSchemaObjectCounts(
        int tables,
        int views,
        int functions,
        int procedures,
        int sequences
) {
}
