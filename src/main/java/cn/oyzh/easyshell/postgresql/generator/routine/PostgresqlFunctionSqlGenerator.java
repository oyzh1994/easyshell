package cn.oyzh.easyshell.postgresql.generator.routine;

import cn.oyzh.easyshell.postgresql.function.PostgresqlCreateFunctionParam;
import cn.oyzh.easyshell.postgresql.function.PostgresqlFunction;
import cn.oyzh.easyshell.postgresql.generator.function.PostgresqlFunctionCreateSqlGenerator;
import cn.oyzh.fx.db.DBSqlGenerator;

/**
 * 函数sql生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class PostgresqlFunctionSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param function 函数
     */
    private void _generate(PostgresqlFunction function) {
        PostgresqlCreateFunctionParam param = new PostgresqlCreateFunctionParam();
        param.setSchema(function.getSchema());
        param.setFunction(function);
        this.sqlBuilder.append(PostgresqlFunctionCreateSqlGenerator.generateSqlSingle(param));
    }

    /**
     * 生成单条SQL
     *
     * @param param 函数
     * @return SQL语句
     */
    public String generateSingle(PostgresqlFunction param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 函数
     * @return SQL语句
     */
    public static String generateSqlSingle(PostgresqlFunction param) {
        return new PostgresqlFunctionSqlGenerator().generateSingle(param);
    }
}
