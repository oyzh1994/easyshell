package cn.oyzh.easyshell.postgresql.generator.function;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.function.PostgresqlAlertFunctionParam;
import cn.oyzh.easyshell.postgresql.function.PostgresqlFunction;
import cn.oyzh.easyshell.postgresql.routine.PostgresqlRoutineParam;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL修改函数SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlFunctionAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改函数参数
     */
    private void _generate(PostgresqlAlertFunctionParam param) {
        PostgresqlFunction function = param.getFunction();
        String definition = function.getDefinition();
        // 函数体已是完整的CREATE语句，原样返回
        if (StringUtil.isNotBlank(definition) && StringUtil.startWithIgnoreCase(definition.trim(), "CREATE ")) {
            this.sqlBuilder.append(definition.trim());
            return;
        }
        String schema = StringUtil.isNotBlank(function.getSchema()) ? function.getSchema() : param.getSchema();
        // PostgreSQL 的 CREATE OR REPLACE FUNCTION 不能修改输入参数签名/名称，
        // 参数有变化时先删除旧签名，再创建新函数。
        if (this.signatureChanged(function)) {
            this.sqlBuilder.append(this.dropFunctionSql(schema, function)).append("\n");
        }
        this.sqlBuilder.append("CREATE OR REPLACE FUNCTION ");
        this.sqlBuilder.append(DBUtil.wrap(schema, function.getName(), DBDialect.POSTGRESQL));
        // 参数
        this.sqlBuilder.append(" (");
        List<PostgresqlRoutineParam> params = function.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (PostgresqlRoutineParam routineParam : params) {
                this.sqlBuilder.append("\n")
                        .append(this.parameterDefinition(routineParam))
                        .append(",");
            }
        }
        StringUtil.deleteLast(this.sqlBuilder, ",");
        this.sqlBuilder.append("\n) ");
        // 返回值：存在 OUT/INOUT 参数时由输出参数决定
        this.sqlBuilder.append("\nRETURNS ").append(this.returnType(function));
        // 语言
        this.sqlBuilder.append("\nLANGUAGE plpgsql");
        // 函数特性
        String characteristic = function.getCharacteristic();
        if (StringUtil.isNotBlank(characteristic)) {
            String upper = characteristic.toUpperCase();
            if (upper.contains("DETERMINISTIC") || upper.contains("IMMUTABLE")) {
                this.sqlBuilder.append("\nIMMUTABLE");
            } else if (upper.contains("STABLE")) {
                this.sqlBuilder.append("\nSTABLE");
            } else if (upper.contains("VOLATILE")) {
                this.sqlBuilder.append("\nVOLATILE");
            }
            if (upper.contains("STRICT")) {
                this.sqlBuilder.append("\nSTRICT");
            }
        }
        // 安全类型
        if (StringUtil.isNotBlank(function.getSecurityType())) {
            String securityType = function.getSecurityType().trim().toUpperCase();
            if (securityType.contains("DEFINER")) {
                this.sqlBuilder.append("\nSECURITY DEFINER");
            } else if (securityType.contains("INVOKER") || securityType.contains("CURRENT_USER")) {
                this.sqlBuilder.append("\nSECURITY INVOKER");
            }
        }
        // 并行安全
        if (StringUtil.isNotBlank(characteristic) && StringUtil.containsIgnoreCase(characteristic, "PARALLEL_ENABLE")) {
            this.sqlBuilder.append("\nPARALLEL SAFE");
        }
        // COST/ROWS 原样保留
        if (StringUtil.isNotBlank(characteristic)) {
            for (String item : characteristic.split(",")) {
                String token = item.trim().toUpperCase();
                if (token.startsWith("COST ") || token.startsWith("ROWS ")) {
                    this.sqlBuilder.append("\n").append(token);
                }
            }
        }
        // 函数体
        this.sqlBuilder.append("\nAS $$\n")
                .append(definition == null ? "" : definition.trim())
                .append("\n$$;");
    }

    /**
     * 输入参数签名是否发生变化
     *
     * @param function 函数
     * @return 结果
     */
    private boolean signatureChanged(PostgresqlFunction function) {
        if (function.getOriginalParams() == null) {
            return false;
        }
        if (!this.signature(function.getOriginalParams(), true)
                .equals(this.signature(function.getParams(), false))) {
            return true;
        }
        // 没有 OUT/INOUT 参数时，返回类型变化也需要重建函数
        if (!this.hasOutputParam(function.getOriginalParams())
                && !this.hasOutputParam(function.getParams())) {
            return !this.returnSignature(function, true)
                    .equals(this.returnSignature(function, false));
        }
        return false;
    }

    /**
     * 是否存在 OUT/INOUT 参数
     *
     * @param params 参数列表
     * @return 结果
     */
    private boolean hasOutputParam(List<PostgresqlRoutineParam> params) {
        if (params != null) {
            for (PostgresqlRoutineParam param : params) {
                if (!param.isDeleted() && this.isOutputParam(param.getMode())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取返回类型签名
     *
     * @param function 函数
     * @param original 是否使用原始值
     * @return 签名
     */
    private String returnSignature(PostgresqlFunction function, boolean original) {
        PostgresqlRoutineParam returnParam = original ? function.getOriginalReturnParam() : function.getReturnParam();
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
    private List<String> signature(List<PostgresqlRoutineParam> params, boolean original) {
        List<String> signature = new ArrayList<>();
        if (params == null) {
            return signature;
        }
        for (PostgresqlRoutineParam param : params) {
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
    private String originalValue(PostgresqlRoutineParam param, String key, String current) {
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
            case "CHARACTER VARYING", "VARCHAR" -> "VARCHAR";
            case "CHARACTER", "BPCHAR", "CHAR" -> "CHAR";
            case "TIMESTAMP WITHOUT TIME ZONE", "TIMESTAMP" -> "TIMESTAMP";
            case "TIME WITHOUT TIME ZONE", "TIME" -> "TIME";
            case "INT2", "SMALLINT", "SMALLSERIAL" -> "SMALLINT";
            case "INT4", "INTEGER", "INT", "SERIAL" -> "INTEGER";
            case "INT8", "BIGINT", "BIGSERIAL" -> "BIGINT";
            case "FLOAT4", "REAL" -> "REAL";
            case "FLOAT8", "DOUBLE PRECISION" -> "DOUBLE PRECISION";
            case "BOOL", "BOOLEAN" -> "BOOLEAN";
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
    private String dropFunctionSql(String schema, PostgresqlFunction function) {
        List<String> args = new ArrayList<>();
        List<PostgresqlRoutineParam> params = function.getOriginalParams();
        if (params != null) {
            for (PostgresqlRoutineParam param : params) {
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
                + DBUtil.wrap(schema, function.getName(), DBDialect.POSTGRESQL)
                + "(" + String.join(", ", args) + ");";
    }

    /**
     * 解析函数返回类型。存在 OUT/INOUT 参数时，PostgreSQL 要求：
     * 单个输出参数使用该参数类型，多个输出参数使用 record。
     *
     * @param function 函数
     * @return 返回类型
     */
    private String returnType(PostgresqlFunction function) {
        List<PostgresqlRoutineParam> outputs = new ArrayList<>();
        List<PostgresqlRoutineParam> params = function.getParams();
        if (params != null) {
            for (PostgresqlRoutineParam param : params) {
                if (!param.isDeleted() && this.isOutputParam(param.getMode())) {
                    outputs.add(param);
                }
            }
        }
        if (outputs.size() == 1) {
            String type = this.typeDefinition(outputs.get(0));
            return StringUtil.isBlank(type) ? "record" : type;
        }
        if (outputs.size() > 1) {
            return "record";
        }
        PostgresqlRoutineParam returnParam = function.getReturnParam();
        String type = returnParam == null ? null : this.typeDefinition(returnParam);
        return StringUtil.isBlank(type) ? "void" : type;
    }

    /**
     * 是否为输出参数
     *
     * @param mode 模式
     * @return 结果
     */
    private boolean isOutputParam(String mode) {
        return StringUtil.containsIgnoreCase(mode, "OUT");
    }

    /**
     * 获取参数定义，PostgreSQL要求参数模式位于参数名之前
     *
     * @param param 参数
     * @return 参数定义
     */
    private String parameterDefinition(PostgresqlRoutineParam param) {
        StringBuilder builder = new StringBuilder();
        String mode = param.getMode();
        if (StringUtil.isNotBlank(mode)) {
            String upper = mode.trim().toUpperCase();
            if (upper.contains("IN") && upper.contains("OUT")) {
                builder.append("INOUT ");
            } else if (upper.contains("OUT")) {
                builder.append("OUT ");
            } else if (upper.contains("IN")) {
                builder.append("IN ");
            }
        }
        if (StringUtil.isNotBlank(param.getName())) {
            builder.append(DBUtil.wrap(param.getName(), DBDialect.POSTGRESQL)).append(" ");
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
    private String typeDefinition(PostgresqlRoutineParam param) {
        String type = param.getType();
        if (StringUtil.isBlank(type)) {
            return "";
        }
        StringBuilder builder = new StringBuilder(type);
        if (DBColumnFieldManager.supportSize(DBDialect.POSTGRESQL, type) && param.getSize() != null) {
            builder.append("(").append(param.getSize());
            if (DBColumnFieldManager.supportDigits(DBDialect.POSTGRESQL, type) && param.getDigits() != null) {
                builder.append(",").append(param.getDigits());
            }
            builder.append(")");
        } else if (DBColumnFieldManager.supportValue(DBDialect.POSTGRESQL, type) && param.getValue() != null) {
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
    public String generateSingle(PostgresqlAlertFunctionParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改函数参数
     * @return SQL语句
     */
    public static String generateSqlSingle(PostgresqlAlertFunctionParam param) {
        return new PostgresqlFunctionAlertSqlGenerator().generateSingle(param);
    }
}
