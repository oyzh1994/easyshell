package cn.oyzh.easyshell.mariadb.generator.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.view.MariadbAlertViewParam;
import cn.oyzh.easyshell.mariadb.view.MariadbView;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

/**
 * MariaDB修改视图SQL生成器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbViewAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改视图参数
     */
    private void _generate(MariadbAlertViewParam param) {
        MariadbView view = param.getView();
        String dbName = param.getDbName();
        this.sqlBuilder.append("CREATE OR REPLACE ");
        if (StringUtil.isNotBlank(view.getAlgorithm())) {
            this.sqlBuilder.append(" ALGORITHM = ")
                    .append(view.getAlgorithm());
        }
        if (StringUtil.isNotBlank(view.getDefiner())) {
            this.sqlBuilder.append(" DEFINER = ")
                    .append(view.getDefiner());
        }
        if (StringUtil.isNotBlank(view.getSecurityType())) {
            this.sqlBuilder.append(" SQL SECURITY ")
                    .append(view.getSecurityType());
        }
        this.sqlBuilder.append(" VIEW ")
                .append(DBUtil.wrap(dbName, view.getName(), DBDialect.MARIADB))
                .append(" AS \n")
                .append(view.getDefinition())
                .append("\n");
        if (view.hasCheckOption()) {
            this.sqlBuilder.append(" WITH ").append(view.getCheckOption()).append(" CHECK OPTION");
        }
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改视图参数
     * @return SQL语句
     */
    public String generateSingle(MariadbAlertViewParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改视图参数
     * @return SQL语句
     */
    public static String generateSqlSingle(MariadbAlertViewParam param) {
        return new MariadbViewAlertSqlGenerator().generateSingle(param);
    }
}
