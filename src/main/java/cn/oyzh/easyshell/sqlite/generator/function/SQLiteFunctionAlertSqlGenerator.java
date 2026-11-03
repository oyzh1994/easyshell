package cn.oyzh.easyshell.sqlite.generator.function;

import cn.oyzh.easyshell.sqlite.function.SQLiteAlertFunctionParam;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.fx.db.DBSqlGenerator;

import java.util.List;
import java.util.Objects;

/**
 * SQLite修改函数SQL生成器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteFunctionAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改函数参数
     */
    private void _generate(SQLiteAlertFunctionParam param) {
        SQLiteFunction function = param == null ? null : param.getFunction();
        if (function != null && SQLiteFunction.KIND_TABLE_VIEW.equals(function.getKind())) {
            this.sqlBuilder.append("DROP VIEW IF EXISTS ")
                    .append(SQLiteGeneratorUtil.quote(Objects.toString(function.getName(), "")))
                    .append(";");
            this.sqlList.add(SQLiteFunctionCreateSqlGenerator.createFunction(function));
            return;
        }
        this.sqlBuilder.append(SQLiteFunctionCreateSqlGenerator.createFunction(function));
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改函数参数
     * @return SQL列表
     */
    public List<String> generate(SQLiteAlertFunctionParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改函数参数
     * @return SQL语句
     */
    public String generateSingle(SQLiteAlertFunctionParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改函数参数
     * @return SQL列表
     */
    public static List<String> generateSql(SQLiteAlertFunctionParam param) {
        return new SQLiteFunctionAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改函数参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SQLiteAlertFunctionParam param) {
        return new SQLiteFunctionAlertSqlGenerator().generateSingle(param);
    }
}
