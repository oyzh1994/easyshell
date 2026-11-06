package cn.oyzh.easyshell.postgresql.generator.routine;

import cn.oyzh.easyshell.postgresql.generator.procedure.PostgresqlProcedureCreateSqlGenerator;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlCreateProcedureParam;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlProcedure;
import cn.oyzh.fx.db.DBSqlGenerator;

/**
 * 存储过程sql生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class PostgresqlProcedureSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param procedure 存储过程
     */
    private void _generate(PostgresqlProcedure procedure) {
        PostgresqlCreateProcedureParam param = new PostgresqlCreateProcedureParam();
        param.setSchema(procedure.getSchema());
        param.setProcedure(procedure);
        this.sqlBuilder.append(PostgresqlProcedureCreateSqlGenerator.generateSqlSingle(param));
    }

    /**
     * 生成单条SQL
     *
     * @param param 存储过程
     * @return SQL语句
     */
    public String generateSingle(PostgresqlProcedure param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 存储过程
     * @return SQL语句
     */
    public static String generateSqlSingle(PostgresqlProcedure param) {
        return new PostgresqlProcedureSqlGenerator().generateSingle(param);
    }
}
