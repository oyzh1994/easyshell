package cn.oyzh.easyshell.sqlserver.generator.procedure;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerCreateProcedureParam;
import cn.oyzh.easyshell.sqlserver.procedure.SqlServerProcedure;
import cn.oyzh.easyshell.sqlserver.routine.SqlServerRoutineParam;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * SqlServer创建存储过程SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerProcedureCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建存储过程参数
     */
    private void _generate(SqlServerCreateProcedureParam param) {
        SqlServerProcedure procedure = param.getProcedure();
        String definition = procedure.getDefinition();
        // 存储过程体已是完整的CREATE语句，原样返回
        if (StringUtil.isNotBlank(definition) && StringUtil.startWithIgnoreCase(definition.trim(), "CREATE ")) {
            this.sqlBuilder.append(definition.trim());
            return;
        }
        String schema = StringUtil.isNotBlank(procedure.getSchema()) ? procedure.getSchema() : param.getSchema();
        this.sqlBuilder.append("CREATE OR ALTER PROCEDURE ");
        this.sqlBuilder.append(DBUtil.wrap(schema, procedure.getName(), DBDialect.SQLSERVER));
        List<SqlServerRoutineParam> params = procedure.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            this.sqlBuilder.append("\n ");
            for (SqlServerRoutineParam routineParam : params) {
                this.sqlBuilder.append(this.parameterDefinition(routineParam)).append(",");
            }
            StringUtil.deleteLast(this.sqlBuilder, ",");
        }
        if (StringUtil.isNotBlank(procedure.getSecurityType())) {
            String securityType = procedure.getSecurityType().trim().toUpperCase();
            if (securityType.contains("OWNER") || securityType.contains("DEFINER")) {
                this.sqlBuilder.append("\nWITH EXECUTE AS OWNER");
            } else if (securityType.contains("SELF")) {
                this.sqlBuilder.append("\nWITH EXECUTE AS SELF");
            } else if (securityType.contains("CALLER") || securityType.contains("INVOKER")) {
                this.sqlBuilder.append("\nWITH EXECUTE AS CALLER");
            }
        }
        this.sqlBuilder.append("\nAS\nBEGIN\n")
                .append(definition == null ? "" : definition.trim())
                .append("\nEND;");
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
            String name = param.getName().replace("@", "");
            builder.append("@").append(name).append(" ");
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
     * 生成单条SQL
     *
     * @param param 创建存储过程参数
     * @return SQL语句
     */
    public String generateSingle(SqlServerCreateProcedureParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建存储过程参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SqlServerCreateProcedureParam param) {
        return new SqlServerProcedureCreateSqlGenerator().generateSingle(param);
    }
}
