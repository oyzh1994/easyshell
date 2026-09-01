package cn.oyzh.easyshell.mysql.generator.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.easyshell.mysql.view.MysqlAlertViewParam;
import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.easyshell.util.db.ShellDBUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;

/**
 * @author oyzh
 * @since 2024/09/11
 */
public class MysqlViewAlertSqlGenerator extends DBSqlGenerator {

    private void _generate(MysqlAlertViewParam param) {
        MysqlView view = param.getView();
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
                .append(ShellDBUtil.wrap(dbName, view.getName(), DBDialect.MYSQL))
                .append(" AS \n")
                .append(view.getDefinition())
                .append("\n");
        if (view.hasCheckOption()) {
            this.sqlBuilder.append(" WITH ").append(view.getCheckOption()).append(" CHECK OPTION");
        }
    }

    public String generateSingle(MysqlAlertViewParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    public static String generateSqlSingle(MysqlAlertViewParam param) {
        return new MysqlViewAlertSqlGenerator().generateSingle(param);
    }
}
