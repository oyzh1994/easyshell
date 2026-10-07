package cn.oyzh.easyshell.dameng.generator.procedure;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.procedure.DamengCreateProcedureParam;
import cn.oyzh.easyshell.dameng.procedure.DamengProcedure;
import cn.oyzh.easyshell.dameng.routine.DamengRoutineParam;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * 达梦创建存储过程SQL生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class DamengProcedureCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建存储过程参数
     */
    private void _generate(DamengCreateProcedureParam param) {
        DamengProcedure procedure = param.getProcedure();
        this.sqlBuilder.append("CREATE PROCEDURE ");
        this.sqlBuilder.append(DBUtil.wrap(procedure.getName(), DBDialect.DAMENG));
        // 参数
        this.sqlBuilder.append(" (");
        List<DamengRoutineParam> params = procedure.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (DamengRoutineParam routineParam : params) {
                this.sqlBuilder.append("\n")
                        .append(routineParam.getDefinition())
                        .append(",");
            }
        }
        StringUtil.deleteLast(this.sqlBuilder, ",");
        this.sqlBuilder.append("\n) ");
        String characteristic = procedure.getCharacteristic();
        if (StringUtil.isNotBlank(characteristic)) {
            if (characteristic.contains("PARALLEL_ENABLE")) {
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
     * 生成单条SQL
     *
     * @param param 创建存储过程参数
     * @return SQL语句
     */
    public String generateSingle(DamengCreateProcedureParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建存储过程参数
     * @return SQL语句
     */
    public static String generateSqlSingle(DamengCreateProcedureParam param) {
        return new DamengProcedureCreateSqlGenerator().generateSingle(param);
    }
}
