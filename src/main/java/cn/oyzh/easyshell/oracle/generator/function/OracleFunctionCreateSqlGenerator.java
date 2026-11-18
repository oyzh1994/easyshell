package cn.oyzh.easyshell.oracle.generator.function;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.function.OracleCreateFunctionParam;
import cn.oyzh.easyshell.oracle.function.OracleFunction;
import cn.oyzh.easyshell.oracle.routine.OracleRoutineParam;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * Oracle创建函数SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleFunctionCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建函数参数
     */
    private void _generate(OracleCreateFunctionParam param) {
        OracleFunction function = param.getFunction();
        this.sqlBuilder.append("CREATE FUNCTION ");
        this.sqlBuilder.append(DBUtil.wrap(function.getSchema(), function.getName(), DBDialect.ORACLE));
        // 参数
        List<OracleRoutineParam> params = function.getParams();
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
        // 返回值
        OracleRoutineParam returnParam = function.getReturnParam();
        if (returnParam != null) {
            this.sqlBuilder.append(" \nRETURN ")
                    .append(returnParam.getTypeDefinition());
        }
        String characteristic = function.getCharacteristic();
        if (StringUtil.isNotBlank(characteristic)) {
            if (StringUtil.containsIgnoreCase(characteristic, "PIPELINED")) {
                this.sqlBuilder.append(" \nPIPELINED");
            }
            if (StringUtil.containsIgnoreCase(characteristic, "PARALLEL_ENABLE")) {
                this.sqlBuilder.append(" \nPARALLEL_ENABLE");
            }
            if (StringUtil.containsIgnoreCase(characteristic, "DETERMINISTIC")) {
                this.sqlBuilder.append(" \nDETERMINISTIC");
            }
        }
        if (StringUtil.isNotBlank(function.getSecurityType())) {
            this.sqlBuilder.append(" \nAUTHID ")
                    .append(function.getSecurityType());
        }
        if (StringUtil.isNotBlank(characteristic)) {
            if (StringUtil.containsIgnoreCase(characteristic, "RESULT_CACHE")) {
                this.sqlBuilder.append(" \nRESULT_CACHE");
            }
        }
        // 函数体
        this.sqlBuilder.append(" \nAS\n")
                .append(function.getDefinition());
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建函数参数
     * @return SQL语句
     */
    public String generateSingle(OracleCreateFunctionParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建函数参数
     * @return SQL语句
     */
    public static String generateSqlSingle(OracleCreateFunctionParam param) {
        return new OracleFunctionCreateSqlGenerator().generateSingle(param);
    }
}
