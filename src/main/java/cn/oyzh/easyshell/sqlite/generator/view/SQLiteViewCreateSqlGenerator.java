package cn.oyzh.easyshell.sqlite.generator.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.view.SQLiteCreateViewParam;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.fx.db.DBSqlGenerator;

import java.util.List;

/**
 * SQLite创建视图SQL生成器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteViewCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建视图参数
     */
    private void _generate(SQLiteCreateViewParam param) {
        this.sqlBuilder.append(createView(param.getViewName(), param.getView().getDefinition()));
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建视图参数
     * @return SQL列表
     */
    public List<String> generate(SQLiteCreateViewParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建视图参数
     * @return SQL语句
     */
    public String generateSingle(SQLiteCreateViewParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建视图参数
     * @return SQL列表
     */
    public static List<String> generateSql(SQLiteCreateViewParam param) {
        return new SQLiteViewCreateSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建视图参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SQLiteCreateViewParam param) {
        return new SQLiteViewCreateSqlGenerator().generateSingle(param);
    }

    /**
     * 创建视图SQL
     *
     * @param name       视图名称
     * @param definition 视图定义
     * @return SQL语句
     */
    public static String createView(String name, String definition) {
        SQLiteGeneratorUtil.requireName(name);
        if (StringUtil.isBlank(definition)) {
            throw new IllegalArgumentException("SQLite view definition is empty");
        }
        return "CREATE VIEW " + SQLiteGeneratorUtil.quote(name) + " AS\n"
                + SQLiteGeneratorUtil.stripSemicolon(definition) + ";";
    }

    /**
     * 创建视图SQL
     *
     * @param view 视图对象
     * @return SQL语句
     */
    public static String createView(SQLiteView view) {
        return createView(view.getName(), view.getDefinition());
    }

    /**
     * 获取视图定义
     *
     * @param createSql 创建视图SQL
     * @return 视图定义
     */
    public static String viewDefinition(String createSql) {
        if (StringUtil.isBlank(createSql)) {
            return null;
        }
        String sql = SQLiteGeneratorUtil.stripSemicolon(createSql);
        int index = SQLiteGeneratorUtil.keywordIndex(sql, "AS", 0);
        return index < 0 ? sql : sql.substring(index + 2).trim();
    }
}
