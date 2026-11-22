package cn.oyzh.easyshell.sqlserver.generator.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerHelper;
import cn.oyzh.easyshell.sqlserver.view.SqlServerCreateViewParam;
import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * SqlServer创建视图SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class SqlServerViewCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建视图参数
     */
    private void _generate(SqlServerCreateViewParam param) {
        SqlServerView view = param.getView();
        String schema = param.getSchema();
        String viewName = param.getViewName();
        String viewFullName = DBUtil.wrap(schema, viewName, DBDialect.SQLSERVER);

        this.sqlBuilder.append("CREATE OR ALTER VIEW ");
        this.sqlBuilder.append(viewFullName);
        this.sqlBuilder.append(" AS \n")
                .append(this.normalizeDefinition(view.getDefinition()));
        this.sqlBuilder.append(";");
        if (StringUtil.isNotBlank(view.getComment())) {
            this.sqlList.add(this.commentSql(schema, viewName, view.getComment()));
        }
    }

    private String commentSql(String schema, String viewName, Object comment) {
        return "EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value="
                + "N" + DBUtil.wrapString(String.valueOf(comment), DBDialect.SQLSERVER)
                + ", @level0type=N'SCHEMA', @level0name="
                + "N" + DBUtil.wrapString(schema, DBDialect.SQLSERVER)
                + ", @level1type=N'VIEW', @level1name="
                + "N" + DBUtil.wrapString(viewName, DBDialect.SQLSERVER) + ";";
    }

    /**
     * 规范化视图定义，移除末尾分号，避免重复
     *
     * @param definition 视图定义
     * @return 视图定义
     */
    private String normalizeDefinition(String definition) {
        return ShellSqlServerHelper.normalizeViewDefinition(definition);
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建视图参数
     * @return SQL列表
     */
    public List<String> generate(SqlServerCreateViewParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建视图参数
     * @return SQL语句
     */
    public String generateSingle(SqlServerCreateViewParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建视图参数
     * @return SQL列表
     */
    public static List<String> generateSql(SqlServerCreateViewParam param) {
        return new SqlServerViewCreateSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建视图参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SqlServerCreateViewParam param) {
        return new SqlServerViewCreateSqlGenerator().generateSingle(param);
    }
}
