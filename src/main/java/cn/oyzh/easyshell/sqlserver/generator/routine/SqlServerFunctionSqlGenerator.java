package cn.oyzh.easyshell.sqlserver.generator.routine;

import cn.oyzh.easyshell.sqlserver.function.SqlServerCreateFunctionParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.sqlserver.generator.function.SqlServerFunctionCreateSqlGenerator;
import cn.oyzh.fx.db.DBSqlGenerator;

/**
 * 函数sql生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class SqlServerFunctionSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param function 函数
     */
    private void _generate(SqlServerFunction function) {
        SqlServerCreateFunctionParam param = new SqlServerCreateFunctionParam();
        param.setSchema(function.getSchema());
        param.setFunction(function);
        this.sqlBuilder.append(SqlServerFunctionCreateSqlGenerator.generateSqlSingle(param));
    }

    /**
     * 生成单条SQL
     *
     * @param param 函数
     * @return SQL语句
     */
    public String generateSingle(SqlServerFunction param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 函数
     * @return SQL语句
     */
    public static String generateSqlSingle(SqlServerFunction param) {
        return new SqlServerFunctionSqlGenerator().generateSingle(param);
    }
}
