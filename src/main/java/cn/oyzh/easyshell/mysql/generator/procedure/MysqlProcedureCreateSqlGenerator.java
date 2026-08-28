package cn.oyzh.easyshell.mysql.generator.procedure;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.DBDialect;
import cn.oyzh.easyshell.db.DBSqlGenerator;
import cn.oyzh.easyshell.mysql.function.MysqlCreateFunctionParam;
import cn.oyzh.easyshell.mysql.function.MysqlFunction;
import cn.oyzh.easyshell.mysql.generator.function.MysqlFunctionCreateSqlGenerator;
import cn.oyzh.easyshell.mysql.procedure.MysqlCreateProcedureParam;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;

import java.util.List;

/**
 * 函数sql生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class MysqlProcedureCreateSqlGenerator extends DBSqlGenerator {

    private void _generate(MysqlCreateProcedureParam param) {
        String dbName = param.getDbName();
        MysqlProcedure procedure = param.getProcedure();
        this.sqlBuilder.append("CREATE ");
        // 定义者
        if (StringUtil.isNotBlank(procedure.getDefiner())) {
            this.sqlBuilder.append(" DEFINER = ")
                    .append(procedure.getDefiner());
        }
        this.sqlBuilder.append(" PROCEDURE ")
                .append(ShellMysqlUtil.wrap(dbName, procedure.getName(), DBDialect.MYSQL));
        // 参数
        this.sqlBuilder.append(" (");
        List<MysqlRoutineParam> params = procedure.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (MysqlRoutineParam routineParam : params) {
                this.sqlBuilder.append("\n")
                        .append(routineParam.getDefinition(true))
                        .append(",");
            }
        }
        StringUtil.deleteLast(this.sqlBuilder, ",");
        this.sqlBuilder.append(") ");
        // 注释
        if (StringUtil.isNotBlank(procedure.getComment())) {
            this.sqlBuilder.append(" \nCOMMENT ")
                    .append(ShellMysqlUtil.wrapData(procedure.getComment()));
        }
        // 安全性
        if (StringUtil.isNotBlank(procedure.getSecurityType())) {
            this.sqlBuilder.append(" \nSQL SECURITY ")
                    .append(procedure.getSecurityType());
        }
        // 特征
        if (StringUtil.isNotBlank(procedure.getCharacteristic())) {
            this.sqlBuilder.append(" \n")
                    .append(procedure.getCharacteristic());
        }
        this.sqlBuilder.append(" \n")
                .append(procedure.getDefinition());
    }

    public String generateSingle(MysqlCreateProcedureParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    public static String generateSqlSingle(MysqlCreateProcedureParam param) {
        return new MysqlProcedureCreateSqlGenerator().generateSingle(param);
    }
}
