package cn.oyzh.easyshell.oracle.generator.procedure;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.procedure.OracleAlertProcedureParam;
import cn.oyzh.easyshell.oracle.procedure.OracleProcedure;
import cn.oyzh.easyshell.oracle.routine.OracleRoutineParam;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * Oracle修改存储过程SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleProcedureAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改存储过程参数
     */
    private void _generate(OracleAlertProcedureParam param) {
        OracleProcedure procedure = param.getProcedure();
        this.sqlBuilder.append("CREATE OR REPLACE PROCEDURE ");
        this.sqlBuilder.append(DBUtil.wrap(procedure.getSchema(), procedure.getName(), DBDialect.ORACLE));
        // 参数
        List<OracleRoutineParam> params = procedure.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            this.sqlBuilder.append(" (");
            for (OracleRoutineParam routineParam : params) {
                this.sqlBuilder.append("\n")
                        .append(routineParam.getDefinition())
                        .append(",");
            }
            StringUtil.deleteLast(this.sqlBuilder, ",");
            this.sqlBuilder.append("\n) ");
        } else {
            this.sqlBuilder.append(" ");
        }
        String characteristic = procedure.getCharacteristic();
        if (StringUtil.isNotBlank(characteristic)) {
            if (StringUtil.containsIgnoreCase(characteristic, "PARALLEL_ENABLE")) {
                this.sqlBuilder.append(" \nPARALLEL_ENABLE");
            }
        }
        if (StringUtil.isNotBlank(procedure.getSecurityType())) {
            this.sqlBuilder.append(" \nAUTHID ")
                    .append(procedure.getSecurityType());
        }
        // 存储过程体
        this.sqlBuilder.append(" \nAS\n")
                .append(procedure.getDefinition());
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改存储过程参数
     * @return SQL列表
     */
    public List<String> generate(OracleAlertProcedureParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改存储过程参数
     * @return SQL语句
     */
    public String generateSingle(OracleAlertProcedureParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改存储过程参数
     * @return SQL列表
     */
    public static List<String> generateSql(OracleAlertProcedureParam param) {
        return new OracleProcedureAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改存储过程参数
     * @return SQL语句
     */
    public static String generateSqlSingle(OracleAlertProcedureParam param) {
        return new OracleProcedureAlertSqlGenerator().generateSingle(param);
    }
}
