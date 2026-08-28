package cn.oyzh.easyshell.mysql.generator.procedure;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.DBDialect;
import cn.oyzh.easyshell.db.DBSqlGenerator;
import cn.oyzh.easyshell.mysql.procedure.MysqlAlertProcedureParam;
import cn.oyzh.easyshell.mysql.procedure.MysqlProcedure;
import cn.oyzh.easyshell.mysql.routine.MysqlRoutineParam;
import cn.oyzh.easyshell.util.db.ShellDBUtil;

import java.util.List;

/**
 * 函数sql生成器
 *
 * @author oyzh
 * @since 2024/08/09
 */
public class MysqlProcedureAlertSqlGenerator extends DBSqlGenerator {

    private void _generate(MysqlAlertProcedureParam param) {
        String fullName = ShellDBUtil.wrap(param.getDbName(), param.getProcedureName(), DBDialect.MYSQL);
        MysqlProcedure procedure = param.getProcedure();
        // 删除
        StringBuilder builder = new StringBuilder("DROP PROCEDURE IF EXISTS ");
        builder.append(fullName);
        builder.append(";");
        this.sqlList.add(builder.toString());
        StringUtil.clear(builder);

        builder.append("CREATE ");
        // 定义者
        if (StringUtil.isNotBlank(procedure.getDefiner())) {
            builder.append(" DEFINER = ")
                    .append(procedure.getDefiner());
        }
        builder.append(" PROCEDURE ")
                .append(fullName);
        // 参数
        builder.append(" (");
        List<MysqlRoutineParam> params = procedure.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (MysqlRoutineParam routineParam : params) {
                builder.append("\n")
                        .append(routineParam.getDefinition(true))
                        .append(",");
            }
        }
        StringUtil.deleteLast(builder, ",");
        builder.append("\n) ");
        // 注释
        if (StringUtil.isNotBlank(procedure.getComment())) {
            builder.append(" \nCOMMENT ")
                    .append(ShellDBUtil.wrapData(procedure.getComment(), DBDialect.MYSQL));
        }
        // 安全性
        if (StringUtil.isNotBlank(procedure.getSecurityType())) {
            builder.append(" \nSQL SECURITY ")
                    .append(procedure.getSecurityType());
        }
        // 特征
        if (StringUtil.isNotBlank(procedure.getCharacteristic())) {
            builder.append(" \n")
                    .append(procedure.getCharacteristic());
        }
        // 定义
        builder.append(" \n")
                .append(procedure.getDefinition());
        this.sqlList.add(builder.toString());
    }

    public List<String> generate(MysqlAlertProcedureParam param) {
        this._generate(param);
        return this.buildSql();
    }

    public String generateSingle(MysqlAlertProcedureParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    public static List<String> generateSql(MysqlAlertProcedureParam param) {
        return new MysqlProcedureAlertSqlGenerator().generate(param);
    }

    public static String generateSqlSingle(MysqlAlertProcedureParam param) {
        return new MysqlProcedureAlertSqlGenerator().generateSingle(param);
    }
}
