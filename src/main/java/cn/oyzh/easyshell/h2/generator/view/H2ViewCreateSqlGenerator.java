package cn.oyzh.easyshell.h2.generator.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.view.H2CreateViewParam;
import cn.oyzh.easyshell.h2.view.H2View;
import cn.oyzh.fx.db.DBSqlGenerator;

import java.util.List;

/**
 * H2创建视图SQL生成器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class H2ViewCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建视图参数
     */
    private void _generate(H2CreateViewParam param) {
        this.sqlBuilder.append(createView(
                param.getView().getDbName(), param.getViewName(), param.getView().getDefinition()));
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建视图参数
     * @return SQL列表
     */
    public List<String> generate(H2CreateViewParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建视图参数
     * @return SQL语句
     */
    public String generateSingle(H2CreateViewParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 创建视图参数
     * @return SQL列表
     */
    public static List<String> generateSql(H2CreateViewParam param) {
        return new H2ViewCreateSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建视图参数
     * @return SQL语句
     */
    public static String generateSqlSingle(H2CreateViewParam param) {
        return new H2ViewCreateSqlGenerator().generateSingle(param);
    }

    /**
     * 创建视图SQL
     *
     * @param name       视图名称
     * @param definition 视图定义
     * @return SQL语句
     */
    public static String createView(String name, String definition) {
        return createView(null, name, definition);
    }

    /**
     * 在指定模式中创建视图。
     *
     * @param schema     模式名称
     * @param name       视图名称
     * @param definition 视图定义
     * @return SQL 语句
     */
    public static String createView(String schema, String name, String definition) {
        H2GeneratorUtil.requireName(name);
        if (StringUtil.isBlank(definition)) {
            throw new IllegalArgumentException("H2 view definition is empty");
        }
        return createViewSql(schema, name, definition);
    }

    /**
     * 创建视图预览SQL，必要信息为空时使用默认值。
     *
     * @param name       视图名称
     * @param definition 视图定义
     * @return SQL语句
     */
    public static String createViewPreview(String name, String definition) {
        String viewName = StringUtil.blankToDefault(name, "Unnamed_View");
        String viewDefinition = StringUtil.blankToDefault(definition, "SELECT 1");
        viewDefinition = StringUtil.blankToDefault(
                H2GeneratorUtil.stripSemicolon(viewDefinition), "SELECT 1");
        return createViewSql(null, viewName, viewDefinition);
    }

    private static String createViewSql(String schema, String name, String definition) {
        return "CREATE VIEW " + H2GeneratorUtil.qualified(schema, name) + " AS\n"
                + H2GeneratorUtil.stripSemicolon(definition) + ";";
    }

    /**
     * 创建视图SQL
     *
     * @param view 视图对象
     * @return SQL语句
     */
    public static String createView(H2View view) {
        return createView(view.getDbName(), view.getName(), view.getDefinition());
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
        String sql = H2GeneratorUtil.stripSemicolon(createSql);
        int index = H2GeneratorUtil.keywordIndex(sql, "AS", 0);
        return index < 0 ? sql : sql.substring(index + 2).trim();
    }
}
