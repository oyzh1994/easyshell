package cn.oyzh.easyshell.sqlserver.generator.procedure;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerHelper;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerAlertProcedureParam;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.easyshell.sqlserver.routine.SqlServerRoutineParam;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * SqlServer修改存储过程SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerProcedureAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改存储过程参数
     */
    private void _generate(SqlServerAlertProcedureParam param) {
        SqlServerProcedure procedure = param.getProcedure();
        String schema = StringUtil.isNotBlank(procedure.getSchema()) ? procedure.getSchema() : param.getSchema();
        String fullName = DBUtil.wrap(schema, procedure.getName(), DBDialect.SQLSERVER);
        String definition = ShellSqlServerHelper.normalizeProcedureDefinition(procedure.getDefinition());

        StringBuilder builder = new StringBuilder();
        builder.append("CREATE OR ALTER PROCEDURE ").append(fullName);
        List<SqlServerRoutineParam> params = procedure.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            builder.append("\n ");
            for (SqlServerRoutineParam routineParam : params) {
                builder.append(this.parameterDefinition(routineParam)).append(",");
            }
            StringUtil.deleteLast(builder, ",");
        }
        if (StringUtil.isNotBlank(procedure.getSecurityType())) {
            String securityType = procedure.getSecurityType().trim().toUpperCase();
            if (securityType.contains("OWNER") || securityType.contains("DEFINER")) {
                builder.append("\nWITH EXECUTE AS OWNER");
            } else if (securityType.contains("SELF")) {
                builder.append("\nWITH EXECUTE AS SELF");
            } else if (securityType.contains("CALLER") || securityType.contains("INVOKER")) {
                builder.append("\nWITH EXECUTE AS CALLER");
            }
        }
        builder.append("\nAS\n");
        if (StringUtil.startWithIgnoreCase(definition, "BEGIN")
                || StringUtil.startWithIgnoreCase(definition, "DECLARE")) {
            builder.append(definition).append(";");
        } else {
            builder.append("BEGIN\n")
                    .append(definition)
                    .append("\nEND;");
        }
        this.sqlList.add(builder.toString());
    }

    /**
     * 获取参数定义，SqlServer要求参数模式位于参数名之前
     *
     * @param param 参数
     * @return 参数定义
     */
    private String parameterDefinition(SqlServerRoutineParam param) {
        StringBuilder builder = new StringBuilder();
        if (StringUtil.isNotBlank(param.getName())) {
            builder.append("@").append(param.getName().replace("@", "")).append(" ");
        }
        builder.append(this.typeDefinition(param));
        String mode = param.getMode();
        if (StringUtil.isNotBlank(mode) && StringUtil.containsIgnoreCase(mode, "OUT")) {
            builder.append(" OUTPUT");
        }
        return builder.toString().trim();
    }

    /**
     * 获取类型定义（类型、长度、小数位、值）
     *
     * @param param 参数
     * @return 类型定义
     */
    private String typeDefinition(SqlServerRoutineParam param) {
        String type = param.getType();
        if (StringUtil.isBlank(type)) {
            return "";
        }
        StringBuilder builder = new StringBuilder(type);
        if (DBColumnFieldManager.supportSize(DBDialect.SQLSERVER, type) && param.getSize() != null) {
            builder.append("(").append(param.getSize());
            if (DBColumnFieldManager.supportDigits(DBDialect.SQLSERVER, type) && param.getDigits() != null) {
                builder.append(",").append(param.getDigits());
            }
            builder.append(")");
        } else if (DBColumnFieldManager.supportValue(DBDialect.SQLSERVER, type) && param.getValue() != null) {
            builder.append("(").append(param.getValue()).append(")");
        }
        return builder.toString();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改存储过程参数
     * @return SQL列表
     */
    public List<String> generate(SqlServerAlertProcedureParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改存储过程参数
     * @return SQL语句
     */
    public String generateSingle(SqlServerAlertProcedureParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改存储过程参数
     * @return SQL列表
     */
    public static List<String> generateSql(SqlServerAlertProcedureParam param) {
        return new SqlServerProcedureAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改存储过程参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SqlServerAlertProcedureParam param) {
        return new SqlServerProcedureAlertSqlGenerator().generateSingle(param);
    }
}
