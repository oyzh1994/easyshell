package cn.oyzh.easyshell.postgresql.generator.procedure;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlAlertProcedureParam;
import cn.oyzh.easyshell.postgresql.procedure.PostgresqlProcedure;
import cn.oyzh.easyshell.postgresql.routine.PostgresqlRoutineParam;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * PostgreSQL修改存储过程SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlProcedureAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改存储过程参数
     */
    private void _generate(PostgresqlAlertProcedureParam param) {
        PostgresqlProcedure procedure = param.getProcedure();
        String schema = StringUtil.isNotBlank(procedure.getSchema()) ? procedure.getSchema() : param.getSchema();
        String fullName = DBUtil.wrap(schema, procedure.getName(), DBDialect.POSTGRESQL);
        String definition = procedure.getDefinition();

        // PostgreSQL删除后重建，兼容不支持CREATE OR REPLACE PROCEDURE的版本
        StringBuilder builder = new StringBuilder();
        builder.append("DROP PROCEDURE IF EXISTS ").append(fullName).append(";");
        this.sqlList.add(builder.toString());

        // 存储过程体已是完整的CREATE语句，原样返回
        if (StringUtil.isNotBlank(definition) && StringUtil.startWithIgnoreCase(definition.trim(), "CREATE ")) {
            this.sqlList.add(definition.trim());
            return;
        }

        builder = new StringBuilder();
        builder.append("CREATE PROCEDURE ").append(fullName);
        // 参数
        builder.append(" (");
        List<PostgresqlRoutineParam> params = procedure.getParams();
        if (CollectionUtil.isNotEmpty(params)) {
            for (PostgresqlRoutineParam routineParam : params) {
                builder.append("\n")
                        .append(this.parameterDefinition(routineParam))
                        .append(",");
            }
        }
        StringUtil.deleteLast(builder, ",");
        builder.append("\n) ");
        // 语言
        builder.append("\nLANGUAGE plpgsql");
        // 安全类型
        if (StringUtil.isNotBlank(procedure.getSecurityType())) {
            String securityType = procedure.getSecurityType().trim().toUpperCase();
            if (securityType.contains("DEFINER")) {
                builder.append("\nSECURITY DEFINER");
            } else if (securityType.contains("INVOKER") || securityType.contains("CURRENT_USER")) {
                builder.append("\nSECURITY INVOKER");
            }
        }
        // 存储过程体，PostgreSQL存储过程没有RETURNS
        builder.append("\nAS $$\n")
                .append(definition == null ? "" : definition.trim())
                .append("\n$$;");
        this.sqlList.add(builder.toString());
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
     * 生成SQL列表
     *
     * @param param 修改存储过程参数
     * @return SQL列表
     */
    public List<String> generate(PostgresqlAlertProcedureParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改存储过程参数
     * @return SQL语句
     */
    public String generateSingle(PostgresqlAlertProcedureParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改存储过程参数
     * @return SQL列表
     */
    public static List<String> generateSql(PostgresqlAlertProcedureParam param) {
        return new PostgresqlProcedureAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改存储过程参数
     * @return SQL语句
     */
    public static String generateSqlSingle(PostgresqlAlertProcedureParam param) {
        return new PostgresqlProcedureAlertSqlGenerator().generateSingle(param);
    }
}
