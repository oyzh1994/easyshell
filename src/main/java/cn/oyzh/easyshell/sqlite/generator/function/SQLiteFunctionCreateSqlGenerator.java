package cn.oyzh.easyshell.sqlite.generator.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.function.SQLiteCreateFunctionParam;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.generator.table.SQLiteTableCreateSqlGenerator;
import cn.oyzh.easyshell.sqlite.generator.view.SQLiteViewCreateSqlGenerator;
import cn.oyzh.fx.db.DBSqlGenerator;

/**
 * SQLite 创建函数 SQL 生成器。
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteFunctionCreateSqlGenerator extends DBSqlGenerator {

    public String generateSingle(SQLiteCreateFunctionParam param) {
        this.sqlBuilder.append(createFunction(param.getFunction()));
        return this.buildSqlSingle();
    }

    public static String generateSqlSingle(SQLiteCreateFunctionParam param) {
        return new SQLiteFunctionCreateSqlGenerator().generateSingle(param);
    }

    public static String createFunction(SQLiteFunction function) {
        if (function == null || StringUtil.isBlank(function.getName())) {
            throw new IllegalArgumentException("SQLite function name is empty");
        }
        String kind = StringUtil.blankToDefault(function.getKind(), SQLiteFunction.KIND_SQL_EXPRESSION);
        return switch (kind) {
            case SQLiteFunction.KIND_GENERATED_COLUMN -> {
                if (StringUtil.isBlank(function.getTargetTable())) {
                    throw new IllegalArgumentException("Generated column requires a target table");
                }
                if (StringUtil.isBlank(function.getExpression())) {
                    throw new IllegalArgumentException("Generated column expression is empty");
                }
                String storage = StringUtil.blankToDefault(function.getGeneratedStorage(), "VIRTUAL");
                if (!"VIRTUAL".equalsIgnoreCase(storage) && !"STORED".equalsIgnoreCase(storage)) {
                    throw new IllegalArgumentException("Generated column storage must be VIRTUAL or STORED");
                }
                yield "ALTER TABLE " + SQLiteGeneratorUtil.quote(function.getTargetTable()) + " ADD COLUMN "
                        + SQLiteTableCreateSqlGenerator.columnDefinition(function.toColumn()) + ";";
            }
            case SQLiteFunction.KIND_TABLE_VIEW ->
                    SQLiteViewCreateSqlGenerator.createView(function.getName(), function.getDefinition());
            case SQLiteFunction.KIND_SQL_EXPRESSION -> {
                if (StringUtil.isBlank(function.getExpression())) {
                    throw new IllegalArgumentException("SQL expression is empty");
                }
                yield "SELECT " + function.getExpression() + " AS "
                        + SQLiteGeneratorUtil.quote(function.getName()) + ";";
            }
            default -> throw new IllegalArgumentException("Unsupported SQLite function kind: " + kind);
        };
    }
}
