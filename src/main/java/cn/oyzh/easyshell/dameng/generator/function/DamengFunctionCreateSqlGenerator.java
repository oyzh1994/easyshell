package cn.oyzh.easyshell.dameng.generator.function;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.function.DamengCreateFunctionParam;
import cn.oyzh.easyshell.dameng.function.DamengFunction;
import cn.oyzh.easyshell.dameng.routine.DamengRoutineParam;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * 达梦创建函数SQL生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class DamengFunctionCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建函数参数
     */
    private void _generate(DamengCreateFunctionParam param) {
        DamengFunction function = param.getFunction();
        this.sqlBuilder.append("CREATE FUNCTION ");
        this.sqlBuilder.append(DBUtil.wrap(function.getSchema(), function.getName(), DBDialect.DAMENG));
        // 参数
        this.sqlBuilder.append(" (");
        List<DamengRoutineParam> params = function.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (DamengRoutineParam routineParam : params) {
                this.sqlBuilder.append("\n")
                        .append(routineParam.getDefinition())
                        .append(",");
            }
        }
        StringUtil.deleteLast(this.sqlBuilder, ",");
        this.sqlBuilder.append("\n) ");
        // 返回值
        DamengRoutineParam returnParam = function.getReturnParam();
        if (returnParam != null) {
            this.sqlBuilder.append(" \nRETURN ")
                    .append(returnParam.getDefinition());
        }
        String characteristic = function.getCharacteristic();
        if (StringUtil.isNotBlank(characteristic)) {
            if (characteristic.contains("PIPELINED")) {
                this.sqlBuilder.append(" \nPIPELINED");
            }
            if (characteristic.contains("PARALLEL_ENABLE")) {
                this.sqlBuilder.append(" \nPARALLEL_ENABLE");
            }
            if (characteristic.contains("DETERMINISTIC")) {
                this.sqlBuilder.append(" \nDETERMINISTIC");
            }
        }
        if (StringUtil.isNotBlank(function.getSecurityType())) {
            this.sqlBuilder.append(" \nAUTHID ")
                    .append(function.getSecurityType());
        }
        if (StringUtil.isNotBlank(characteristic)) {
            if (characteristic.contains("RESULT_CACHE")) {
                this.sqlBuilder.append(" \nRESULT_CACHE");
            }
            if (characteristic.contains("AGGREGATE")) {
                this.sqlBuilder.append(" \nAGGREGATE");
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
    public String generateSingle(DamengCreateFunctionParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建函数参数
     * @return SQL语句
     */
    public static String generateSqlSingle(DamengCreateFunctionParam param) {
        return new DamengFunctionCreateSqlGenerator().generateSingle(param);
    }
}
