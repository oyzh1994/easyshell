package cn.oyzh.easyshell.sqlite.generator.view;

import cn.oyzh.easyshell.sqlite.view.SQLiteAlertViewParam;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.fx.db.DBSqlGenerator;

import java.util.List;

/**
 * SQLite修改视图SQL生成器
 *
 * <p>SQLite 使用 DROP + CREATE 表达视图修改，保持与其它数据库生成器一致的分层。</p>
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteViewAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改视图参数
     */
    private void _generate(SQLiteAlertViewParam param) {
        this.sqlBuilder.append("DROP VIEW IF EXISTS ")
                .append(SQLiteGeneratorUtil.quote(param.getView().getName()))
                .append(";");
        this.sqlList.add(SQLiteViewCreateSqlGenerator.createView(param.getView()));
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改视图参数
     * @return SQL列表
     */
    public List<String> generate(SQLiteAlertViewParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改视图参数
     * @return SQL语句
     */
    public String generateSingle(SQLiteAlertViewParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改视图参数
     * @return SQL列表
     */
    public static List<String> generateSql(SQLiteAlertViewParam param) {
        return new SQLiteViewAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改视图参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SQLiteAlertViewParam param) {
        return new SQLiteViewAlertSqlGenerator().generateSingle(param);
    }
}
