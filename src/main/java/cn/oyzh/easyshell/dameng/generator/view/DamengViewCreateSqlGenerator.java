package cn.oyzh.easyshell.dameng.generator.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.view.DamengCreateViewParam;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBSqlGenerator;
import cn.oyzh.fx.db.util.DBUtil;

import java.util.List;

/**
 * 达梦创建视图SQL生成器
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class DamengViewCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建视图参数
     */
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

    /**
     * 生成SQL列表
     *
     * @param param 创建视图参数
     * @return SQL列表
     */
    public List<String> generate(DamengCreateViewParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建视图参数
     * @return SQL语句
     */
    public String generateSingle(DamengCreateViewParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建视图参数
     * @return SQL列表
     */
    public static List<String> generateSql(DamengCreateViewParam param) {
        return new DamengViewCreateSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建视图参数
     * @return SQL语句
     */
    public static String generateSqlSingle(DamengCreateViewParam param) {
        return new DamengViewCreateSqlGenerator().generateSingle(param);
    }
}
