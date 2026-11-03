package cn.oyzh.easyshell.sqlite.generator.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.view.SQLiteCreateViewParam;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.fx.db.DBSqlGenerator;

/**
 * SQLite 创建视图 SQL 生成器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteViewCreateSqlGenerator extends DBSqlGenerator {

    public String generateSingle(SQLiteCreateViewParam param) {
        this.sqlBuilder.append(createView(param.getViewName(), param.getView().getDefinition()));
        return this.buildSqlSingle();
    }

    public static String generateSqlSingle(SQLiteCreateViewParam param) {
        return new SQLiteViewCreateSqlGenerator().generateSingle(param);
    }

    public static String createView(String name, String definition) {
        SQLiteGeneratorUtil.requireName(name);
        if (StringUtil.isBlank(definition)) {
            throw new IllegalArgumentException("SQLite view definition is empty");
        }
        return "CREATE VIEW " + SQLiteGeneratorUtil.quote(name) + " AS\n"
                + SQLiteGeneratorUtil.stripSemicolon(definition) + ";";
    }

    public static String createView(SQLiteView view) {
        return createView(view.getName(), view.getDefinition());
    }

    public static String viewDefinition(String createSql) {
        if (StringUtil.isBlank(createSql)) {
            return null;
        }
        String sql = SQLiteGeneratorUtil.stripSemicolon(createSql);
        int index = SQLiteGeneratorUtil.keywordIndex(sql, "AS", 0);
        return index < 0 ? sql : sql.substring(index + 2).trim();
    }
}
