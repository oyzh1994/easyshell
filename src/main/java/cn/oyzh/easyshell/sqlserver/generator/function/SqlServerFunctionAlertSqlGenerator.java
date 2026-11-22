package cn.oyzh.easyshell.sqlserver.generator.function;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.function.SqlServerAlertFunctionParam;
import cn.oyzh.easyshell.sqlserver.function.SqlServerFunction;
import cn.oyzh.easyshell.sqlserver.routine.SqlServerRoutineParam;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlServer修改函数SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerFunctionAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改函数参数
     */
    private void _generate(SqlServerAlertFunctionParam param) {
        SqlServerFunction function = param.getFunction();
        String definition = function.getDefinition();
        // 函数体已是完整的CREATE语句，原样返回
        if (StringUtil.isNotBlank(definition) && StringUtil.startWithIgnoreCase(definition.trim(), "CREATE ")) {
            this.sqlBuilder.append(definition.trim());
            return;
        }
        String schema = StringUtil.isNotBlank(function.getSchema()) ? function.getSchema() : param.getSchema();
        if (this.signatureChanged(function)) {
            this.sqlBuilder.append("DROP FUNCTION IF EXISTS ")
                    .append(DBUtil.wrap(schema, function.getName(), DBDialect.SQLSERVER))
                    .append(";\n");
        }
        this.sqlBuilder.append("CREATE OR ALTER FUNCTION ");
        this.sqlBuilder.append(DBUtil.wrap(schema, function.getName(), DBDialect.SQLSERVER));
        // 参数
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
        this.sqlBuilder.append("\nAS\nBEGIN\n")
                .append(definition == null ? "" : definition.trim())
                .append("\nEND;");
    }

    /**
     * 输入参数签名是否发生变化
     *
     * @param function 函数
     * @return 结果
     */
    private boolean signatureChanged(SqlServerFunction function) {
        if (function.getOriginalParams() == null) {
            return false;
        }
        if (!this.signature(function.getOriginalParams(), true)
                .equals(this.signature(function.getParams(), false))) {
            return true;
        }
        // SQL Server函数参数只能输入，返回类型变化也需要重建函数
        return !this.returnSignature(function, true)
                .equals(this.returnSignature(function, false));
    }

    /**
     * 获取返回类型签名
     *
     * @param function 函数
     * @param original 是否使用原始值
     * @return 签名
     */
    private String returnSignature(SqlServerFunction function, boolean original) {
        SqlServerRoutineParam returnParam = original ? function.getOriginalReturnParam() : function.getReturnParam();
        if (returnParam == null || StringUtil.isBlank(returnParam.getType())) {
            return "";
        }
        return this.signatureType(returnParam.getType());
    }

    /**
     * 获取输入参数签名
     *
     * @param params   参数列表
     * @param original 是否使用原始值
     * @return 签名列表
     */
    private List<String> signature(List<SqlServerRoutineParam> params, boolean original) {
        List<String> signature = new ArrayList<>();
        if (params == null) {
            return signature;
        }
        for (SqlServerRoutineParam param : params) {
            if (param.isDeleted() || (original && param.isCreated())) {
                continue;
            }
            String mode = original ? this.originalValue(param, "mode", param.getMode()) : param.getMode();
            String type = original ? this.originalValue(param, "type", param.getType()) : param.getType();
            if (StringUtil.isBlank(type)) {
                continue;
            }
            String name = original ? this.originalValue(param, "name", param.getName()) : param.getName();
            signature.add(this.signatureMode(mode) + "|" + this.signatureType(type) + "|" + (name == null ? "" : name));
        }
        return signature;
    }

    /**
     * 获取参数原始值，没有原始值时使用当前值
     *
     * @param param   参数
     * @param key     键
     * @param current 当前值
     * @return 值
     */
    private String originalValue(SqlServerRoutineParam param, String key, String current) {
        if (param.hasOriginalValue(key)) {
            Object value = param.getOriginalValue(key);
            return value == null ? null : value.toString();
        }
        return current;
    }

    /**
     * 用于签名比较的参数模式
     *
     * @param mode 模式
     * @return 模式
     */
    private String signatureMode(String mode) {
        if (StringUtil.isBlank(mode)) {
            return "IN";
        }
        return mode.trim().toUpperCase().replace("IN OUT", "INOUT");
    }

    /**
     * 解析输入参数模式，OUT/TABLE 参数不属于函数签名
     *
     * @param mode 模式
     * @return 输入模式
     */
    private String inputMode(String mode) {
        if (StringUtil.isBlank(mode)) {
            return "IN";
        }
        String upper = mode.trim().toUpperCase();
        if (upper.contains("TABLE")) {
            return null;
        }
        if (upper.contains("VARIADIC")) {
            return "VARIADIC";
        }
        if (upper.contains("IN") && upper.contains("OUT")) {
            return "INOUT";
        }
        if (upper.contains("OUT")) {
            return null;
        }
        return upper.contains("IN") ? "IN" : null;
    }

    /**
     * 用于签名比较的类型名，忽略同义类型差异
     *
     * @param type 类型
     * @return 类型名
     */
    private String signatureType(String type) {
        String upper = type.trim().toUpperCase();
        int index = upper.indexOf('(');
        if (index > 0) {
            upper = upper.substring(0, index).trim();
        }
        return switch (upper) {
            case "VARCHAR" -> "VARCHAR";
            case "CHAR" -> "CHAR";
            case "TIMESTAMP" -> "TIMESTAMP";
            case "TIME" -> "TIME";
            case "SMALLINT" -> "SMALLINT";
            case "INTEGER", "INT" -> "INTEGER";
            case "BIGINT" -> "BIGINT";
            case "REAL" -> "REAL";
            case "DECIMAL", "NUMERIC" -> "NUMERIC";
            default -> upper;
        };
    }

    /**
     * 生成删除旧函数签名的SQL
     *
     * @param schema   模式
     * @param function 函数
     * @return SQL
     */
    private String dropFunctionSql(String schema, SqlServerFunction function) {
        List<String> args = new ArrayList<>();
        List<SqlServerRoutineParam> params = function.getOriginalParams();
        if (params != null) {
            for (SqlServerRoutineParam param : params) {
                if (param.isDeleted() || param.isCreated()) {
                    continue;
                }
                String mode = this.inputMode(this.originalValue(param, "mode", param.getMode()));
                String type = this.originalValue(param, "type", param.getType());
                if (mode == null || StringUtil.isBlank(type)) {
                    continue;
                }
                if ("VARIADIC".equals(mode)) {
                    args.add("VARIADIC " + type);
                } else {
                    args.add(type);
                }
            }
        }
        return "DROP FUNCTION IF EXISTS "
                + DBUtil.wrap(schema, function.getName(), DBDialect.SQLSERVER)
                + "(" + String.join(", ", args) + ");";
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
     * @param param 修改函数参数
     * @return SQL语句
     */
    public String generateSingle(SqlServerAlertFunctionParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改函数参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SqlServerAlertFunctionParam param) {
        return new SqlServerFunctionAlertSqlGenerator().generateSingle(param);
    }
}
