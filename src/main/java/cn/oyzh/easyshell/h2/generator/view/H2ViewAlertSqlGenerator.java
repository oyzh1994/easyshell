package cn.oyzh.easyshell.h2.generator.view;

import cn.oyzh.easyshell.h2.view.H2AlertViewParam;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.fx.db.DBSqlGenerator;

import java.util.List;

/**
 * H2修改视图SQL生成器
 *
 * <p>H2 使用 DROP + CREATE 表达视图修改，保持与其它数据库生成器一致的分层。</p>
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2ViewAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改视图参数
     */
    private void _generate(H2AlertViewParam param) {
        this.sqlBuilder.append("DROP VIEW IF EXISTS ")
                .append(H2GeneratorUtil.qualified(
                        param.getView().getDbName(), param.getView().getName()))
                .append(";");
        this.sqlList.add(H2ViewCreateSqlGenerator.createView(param.getView()));
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改视图参数
     * @return SQL列表
     */
    public List<String> generate(H2AlertViewParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改视图参数
     * @return SQL语句
     */
    public String generateSingle(H2AlertViewParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改视图参数
     * @return SQL列表
     */
    public static List<String> generateSql(H2AlertViewParam param) {
        return new H2ViewAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改视图参数
     * @return SQL语句
     */
    public static String generateSqlSingle(H2AlertViewParam param) {
        return new H2ViewAlertSqlGenerator().generateSingle(param);
    }
}
