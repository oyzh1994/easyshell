package cn.oyzh.easyshell.oracle.generator.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.easyshell.oracle.view.OracleAlertViewParam;
import cn.oyzh.easyshell.oracle.view.OracleView;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * Oracle修改视图SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class OracleViewAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改视图参数
     */
    private void _generate(OracleAlertViewParam param, boolean includeSecurityType) {
        OracleView view = param.getView();
        String schema = param.getSchema();
        String viewName = param.getViewName();
        String viewFullName = DBUtil.wrap(schema, viewName, DBDialect.ORACLE);

        this.sqlBuilder.append("CREATE OR REPLACE ");
        this.sqlBuilder.append(" VIEW ")
                .append(viewFullName);
        if (includeSecurityType && StringUtil.isNotBlank(view.getSecurityType())) {
            this.sqlBuilder.append("\n")
                    .append("BEQUEATH ")
                    .append(view.getSecurityType());
        }
        this.sqlBuilder.append(" AS \n")
                .append(ShellOracleHelper.stripViewReadOnlyClause(view.getDefinition()));
        if (!view.isUpdatable()) {
            this.sqlBuilder.append("\n")
                    .append("WITH READ ONLY");
        }
        this.sqlBuilder.append(";");

        // 注释
        if (StringUtil.isNotBlank(view.getComment())) {
            StringBuilder builder = new StringBuilder();
            builder.append("COMMENT ON TABLE ")
                    .append(viewFullName)
                    .append(" IS ")
                    .append(DBUtil.wrapData(view.getComment(), DBDialect.ORACLE)).append(";");
            this.sqlList.add(builder.toString());
        }
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改视图参数
     * @return SQL列表
     */
    public List<String> generate(OracleAlertViewParam param) {
        this._generate(param, true);
        return this.buildSql();
    }

    /**
     * 生成SQL列表
     *
     * @param param             修改视图参数
     * @param includeSecurityType 是否包含BEQUEATH子句
     * @return SQL列表
     */
    public List<String> generate(OracleAlertViewParam param, boolean includeSecurityType) {
        this._generate(param, includeSecurityType);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改视图参数
     * @return SQL语句
     */
    public String generateSingle(OracleAlertViewParam param) {
        this._generate(param, true);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改视图参数
     * @return SQL列表
     */
    public static List<String> generateSql(OracleAlertViewParam param) {
        return new OracleViewAlertSqlGenerator().generate(param);
    }

    /**
     * 生成SQL列表
     *
     * @param param             修改视图参数
     * @param includeSecurityType 是否包含BEQUEATH子句
     * @return SQL列表
     */
    public static List<String> generateSql(OracleAlertViewParam param, boolean includeSecurityType) {
        return new OracleViewAlertSqlGenerator().generate(param, includeSecurityType);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改视图参数
     * @return SQL语句
     */
    public static String generateSqlSingle(OracleAlertViewParam param) {
        return new OracleViewAlertSqlGenerator().generateSingle(param);
    }
}
