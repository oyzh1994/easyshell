package cn.oyzh.easyshell.dameng.generator.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.view.DamengCreateViewParam;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/11
 */
public class DamengViewCreateSqlGenerator extends DBSqlGenerator {

    private void _generate(DamengCreateViewParam param) {
        DamengView view = param.getView();
        String schema = param.getSchema();
        String viewName = param.getViewName();
        String viewFullName = DBUtil.wrap(schema, viewName, DBDialect.DAMENG);

        this.sqlBuilder.append("CREATE VIEW ");
        this.sqlBuilder.append(viewFullName)
                .append(" AS \n")
                .append(view.getDefinition());
        if (view.getSecurityType() != null) {
            this.sqlBuilder.append("\n")
                    .append("BEQUEATH ")
                    .append(view.getSecurityType());
        }
        if (!view.isUpdatable()) {
            this.sqlBuilder.append("\n")
                    .append("WITH READ ONLY");
        }
        this.sqlBuilder.append(";");

        // 注释
        if (StringUtil.isNotBlank(view.getComment())) {
            StringBuilder builder = new StringBuilder();
            builder.append("COMMENT ON VIEW ")
                    .append(viewFullName)
                    .append(" IS ")
                    .append(DBUtil.wrapData(view.getComment(), DBDialect.DAMENG))
                    .append(";");
            this.sqlList.add(builder.toString());
        }
    }

    public List<String> generate(DamengCreateViewParam param) {
        this._generate(param);
        return this.buildSql();
    }

    public String generateSingle(DamengCreateViewParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    public static List<String> generateSql(DamengCreateViewParam param) {
        return new DamengViewCreateSqlGenerator().generate(param);
    }

    public static String generateSqlSingle(DamengCreateViewParam param) {
        return new DamengViewCreateSqlGenerator().generateSingle(param);
    }
}
