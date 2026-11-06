package cn.oyzh.easyshell.postgresql.generator.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.view.PostgresqlCreateViewParam;
import cn.oyzh.easyshell.postgresql.view.PostgresqlView;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * PostgreSQL创建视图SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class PostgresqlViewCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建视图参数
     */
    private void _generate(PostgresqlCreateViewParam param) {
        PostgresqlView view = param.getView();
        String schema = param.getSchema();
        String viewName = param.getViewName();
        String viewFullName = DBUtil.wrap(schema, viewName, DBDialect.POSTGRESQL);

        this.sqlBuilder.append("CREATE OR REPLACE VIEW ");
        this.sqlBuilder.append(viewFullName);
        // PostgreSQL 15+ 通过 security_invoker 控制视图权限继承，选项位于AS之前
        if (StringUtil.containsIgnoreCase(view.getSecurityType(), "INVOKER")) {
            this.sqlBuilder.append("\nWITH (security_invoker = true)");
        }
        this.sqlBuilder.append(" AS \n")
                .append(this.normalizeDefinition(view.getDefinition()));
        // PostgreSQL不支持 WITH READ ONLY，视图是否可更新由视图定义决定
        this.sqlBuilder.append(";");

        // 注释
        if (StringUtil.isNotBlank(view.getComment())) {
            StringBuilder builder = new StringBuilder();
            builder.append("COMMENT ON VIEW ")
                    .append(viewFullName)
                    .append(" IS ")
                    .append(DBUtil.wrapData(view.getComment(), DBDialect.POSTGRESQL))
                    .append(";");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 规范化视图定义，移除末尾分号，避免重复
     *
     * @param definition 视图定义
     * @return 视图定义
     */
    private String normalizeDefinition(String definition) {
        String sql = definition == null ? "" : definition.trim();
        while (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }
        return sql;
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建视图参数
     * @return SQL列表
     */
    public List<String> generate(PostgresqlCreateViewParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建视图参数
     * @return SQL语句
     */
    public String generateSingle(PostgresqlCreateViewParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建视图参数
     * @return SQL列表
     */
    public static List<String> generateSql(PostgresqlCreateViewParam param) {
        return new PostgresqlViewCreateSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建视图参数
     * @return SQL语句
     */
    public static String generateSqlSingle(PostgresqlCreateViewParam param) {
        return new PostgresqlViewCreateSqlGenerator().generateSingle(param);
    }
}
