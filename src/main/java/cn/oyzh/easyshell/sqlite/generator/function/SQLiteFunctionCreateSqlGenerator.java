package cn.oyzh.easyshell.sqlite.generator.function;

import cn.oyzh.easyshell.sqlite.function.SQLiteCreateFunctionParam;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.fx.db.DBSqlGenerator;

import java.util.Objects;

/**
 * SQLite创建函数SQL生成器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteFunctionCreateSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 创建函数参数
     */
    private void _generate(SQLiteCreateFunctionParam param) {
        this.sqlBuilder.append(createFunction(param == null ? null : param.getFunction()));
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建函数参数
     * @return SQL语句
     */
    public String generateSingle(SQLiteCreateFunctionParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成单条SQL
     *
     * @param param 创建函数参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SQLiteCreateFunctionParam param) {
        return new SQLiteFunctionCreateSqlGenerator().generateSingle(param);
    }

    /**
     * 创建SQLite函数设计SQL
     *
     * @param function 函数对象
     * @return SQL语句
     */
    public static String createFunction(SQLiteFunction function) {
        SQLiteFunction value = function == null ? new SQLiteFunction() : function;
        String kind = Objects.toString(value.getKind(), "");
        String name = SQLiteGeneratorUtil.quote(Objects.toString(value.getName(), ""));
        if (SQLiteFunction.KIND_GENERATED_COLUMN.equals(kind)) {
            StringBuilder sql = new StringBuilder("ALTER TABLE ")
                    .append(SQLiteGeneratorUtil.quote(Objects.toString(value.getTargetTable(), "")))
                    .append(" ADD COLUMN ")
                    .append(name);
            String type = Objects.toString(value.getReturnType(), "");
            if (!type.isEmpty()) {
                sql.append(' ').append(type);
            }
            sql.append(" GENERATED ALWAYS AS (")
                    .append(Objects.toString(value.getExpression(), ""))
                    .append(')');
            String storage = Objects.toString(value.getGeneratedStorage(), "");
            if (!storage.isEmpty()) {
                sql.append(' ').append(storage);
            }
            return sql.append(';').toString();
        }
        if (SQLiteFunction.KIND_TABLE_VIEW.equals(kind)) {
            return "CREATE VIEW " + name + " AS\n"
                    + Objects.toString(value.getDefinition(), "") + ";";
        }
        return "SELECT " + Objects.toString(value.getExpression(), "") + " AS " + name + ";";
    }
}
