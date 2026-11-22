package cn.oyzh.easyshell.sqlserver.generator.function;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerHelper;
import cn.oyzh.easyshell.sqlserver.function.SqlServerCreateFunctionParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.sqlserver.routine.SqlServerRoutineParam;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * SqlServer创建函数SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerFunctionCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建函数参数
     */
    private void _generate(SqlServerCreateFunctionParam param) {
        SqlServerFunction function = param.getFunction();
        String definition = ShellSqlServerHelper.normalizeFunctionDefinition(function.getDefinition());
        String schema = StringUtil.isNotBlank(function.getSchema()) ? function.getSchema() : param.getSchema();
        this.sqlBuilder.append("CREATE OR ALTER FUNCTION ");
        this.sqlBuilder.append(DBUtil.wrap(schema, function.getName(), DBDialect.SQLSERVER));
        this.sqlBuilder.append(" (");
        List<SqlServerRoutineParam> params = function.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (SqlServerRoutineParam routineParam : params) {
                this.sqlBuilder.append("\n")
                        .append(this.parameterDefinition(routineParam))
                        .append(",");
            }
        }
        StringUtil.deleteLast(this.sqlBuilder, ",");
        this.sqlBuilder.append("\n)\nRETURNS ").append(this.returnType(function));
        if (StringUtil.isNotBlank(function.getSecurityType())) {
            String securityType = function.getSecurityType().trim().toUpperCase();
            if (securityType.contains("OWNER") || securityType.contains("DEFINER")) {
                this.sqlBuilder.append("\nWITH EXECUTE AS OWNER");
            } else if (securityType.contains("SELF")) {
                this.sqlBuilder.append("\nWITH EXECUTE AS SELF");
            } else if (securityType.contains("CALLER") || securityType.contains("INVOKER")) {
                this.sqlBuilder.append("\nWITH EXECUTE AS CALLER");
            }
        }
        this.sqlBuilder.append("\nAS\n");
        if (StringUtil.startWithIgnoreCase(definition, "BEGIN")
                || StringUtil.startWithIgnoreCase(definition, "DECLARE")) {
            this.sqlBuilder.append(definition).append(";");
        } else {
            this.sqlBuilder.append("BEGIN\n")
                    .append(definition)
                    .append("\nEND;");
        }
    }

    /**
     * 解析函数返回类型。存在 OUT/INOUT 参数时，SqlServer 要求：
     * 单个输出参数使用该参数类型，多个输出参数使用 record。
     *
     * @param function 函数
     * @return 返回类型
     */
    private String returnType(SqlServerFunction function) {
        SqlServerRoutineParam returnParam = function.getReturnParam();
        String type = returnParam == null ? null : this.typeDefinition(returnParam);
        return StringUtil.isBlank(type) || StringUtil.equalsIgnoreCase(type, "RECORD")
                ? "INT" : type;
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
     * @param param 创建函数参数
     * @return SQL语句
     */
    public String generateSingle(SqlServerCreateFunctionParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建函数参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SqlServerCreateFunctionParam param) {
        return new SqlServerFunctionCreateSqlGenerator().generateSingle(param);
    }
}
