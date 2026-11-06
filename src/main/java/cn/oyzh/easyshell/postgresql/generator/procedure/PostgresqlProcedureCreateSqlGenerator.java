package cn.oyzh.easyshell.postgresql.generator.procedure;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlCreateProcedureParam;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlProcedure;
import cn.oyzh.easyshell.postgresql.routine.PostgresqlRoutineParam;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * PostgreSQL创建存储过程SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlProcedureCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建存储过程参数
     */
    private void _generate(PostgresqlCreateProcedureParam param) {
        PostgresqlProcedure procedure = param.getProcedure();
        String definition = procedure.getDefinition();
        // 存储过程体已是完整的CREATE语句，原样返回
        if (StringUtil.isNotBlank(definition) && StringUtil.startWithIgnoreCase(definition.trim(), "CREATE ")) {
            this.sqlBuilder.append(definition.trim());
            return;
        }
        String schema = StringUtil.isNotBlank(procedure.getSchema()) ? procedure.getSchema() : param.getSchema();
        this.sqlBuilder.append("CREATE PROCEDURE ");
        this.sqlBuilder.append(DBUtil.wrap(schema, procedure.getName(), DBDialect.POSTGRESQL));
        // 参数
        this.sqlBuilder.append(" (");
        List<PostgresqlRoutineParam> params = procedure.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (PostgresqlRoutineParam routineParam : params) {
                this.sqlBuilder.append("\n")
                        .append(this.parameterDefinition(routineParam))
                        .append(",");
            }
        }
        StringUtil.deleteLast(this.sqlBuilder, ",");
        this.sqlBuilder.append("\n) ");
        // 语言
        this.sqlBuilder.append("\nLANGUAGE plpgsql");
        // 安全类型
        if (StringUtil.isNotBlank(procedure.getSecurityType())) {
            String securityType = procedure.getSecurityType().trim().toUpperCase();
            if (securityType.contains("DEFINER")) {
                this.sqlBuilder.append("\nSECURITY DEFINER");
            } else if (securityType.contains("INVOKER") || securityType.contains("CURRENT_USER")) {
                this.sqlBuilder.append("\nSECURITY INVOKER");
            }
        }
        // 存储过程体，PostgreSQL存储过程没有RETURNS
        this.sqlBuilder.append("\nAS $$\n")
                .append(definition == null ? "" : definition.trim())
                .append("\n$$;");
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
     * @param param 创建存储过程参数
     * @return SQL语句
     */
    public String generateSingle(PostgresqlCreateProcedureParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建存储过程参数
     * @return SQL语句
     */
    public static String generateSqlSingle(PostgresqlCreateProcedureParam param) {
        return new PostgresqlProcedureCreateSqlGenerator().generateSingle(param);
    }
}
