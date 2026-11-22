package cn.oyzh.easyshell.sqlserver.generator.routine;

import cn.oyzh.easyshell.sqlserver.generator.procedure.SqlServerProcedureCreateSqlGenerator;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerCreateProcedureParam;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.fx.db.DBSqlGenerator;

/**
 * 存储过程sql生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class SqlServerProcedureSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param procedure 存储过程
     */
    private void _generate(SqlServerProcedure procedure) {
        SqlServerCreateProcedureParam param = new SqlServerCreateProcedureParam();
        param.setSchema(procedure.getSchema());
        param.setProcedure(procedure);
        this.sqlBuilder.append(SqlServerProcedureCreateSqlGenerator.generateSqlSingle(param));
    }

    /**
     * 生成单条SQL
     *
     * @param param 存储过程
     * @return SQL语句
     */
    public String generateSingle(SqlServerProcedure param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 存储过程
     * @return SQL语句
     */
    public static String generateSqlSingle(SqlServerProcedure param) {
        return new SqlServerProcedureSqlGenerator().generateSingle(param);
    }
}
