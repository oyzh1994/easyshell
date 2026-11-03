package cn.oyzh.easyshell.sqlite.generator.function;

import cn.oyzh.easyshell.sqlite.function.SQLiteAlertFunctionParam;
import cn.oyzh.fx.db.DBSqlGenerator;

/**
 * SQLite 修改函数 SQL 生成器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteFunctionAlertSqlGenerator extends DBSqlGenerator {

    public String generateSingle(SQLiteAlertFunctionParam param) {
        this.sqlBuilder.append(SQLiteFunctionCreateSqlGenerator.createFunction(param.getFunction()));
        return this.buildSqlSingle();
    }

    public static String generateSqlSingle(SQLiteAlertFunctionParam param) {
        return new SQLiteFunctionAlertSqlGenerator().generateSingle(param);
    }
}
