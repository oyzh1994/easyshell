package cn.oyzh.easyshell.sqlite.generator.view;

import cn.oyzh.easyshell.sqlite.view.SQLiteAlertViewParam;
import cn.oyzh.fx.db.DBSqlGenerator;

/**
 * SQLite 修改视图 SQL 生成器。
 *
 * <p>SQLite 使用 DROP + CREATE 表达视图修改，保持与其它数据库生成器一致的分层。</p>
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteViewAlertSqlGenerator extends DBSqlGenerator {

    public String generateSingle(SQLiteAlertViewParam param) {
        this.sqlList.add("DROP VIEW IF EXISTS " + cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil.quote(
                param.getView().getName()) + ";");
        this.sqlBuilder.append(SQLiteViewCreateSqlGenerator.createView(param.getView()));
        return this.buildSqlSingle();
    }

    public static String generateSqlSingle(SQLiteAlertViewParam param) {
        return new SQLiteViewAlertSqlGenerator().generateSingle(param);
    }
}
