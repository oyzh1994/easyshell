package cn.oyzh.easyshell.sqlite.generator.index;

import cn.oyzh.easyshell.sqlite.index.SQLiteIndex;
import cn.oyzh.fx.db.DBSqlGenerator;

/**
 * SQLite 创建索引 SQL 生成器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteIndexCreateSqlGenerator extends DBSqlGenerator {

    public String generateSingle(SQLiteIndex index) {
        this.sqlBuilder.append(index.normalizedDefinition(null, index.getTableName()));
        return this.buildSqlSingle();
    }

    public static String generateSqlSingle(SQLiteIndex index) {
        return new SQLiteIndexCreateSqlGenerator().generateSingle(index);
    }
}
