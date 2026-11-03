package cn.oyzh.easyshell.sqlite.generator.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.generator.SQLiteGeneratorUtil;
import cn.oyzh.easyshell.sqlite.index.SQLiteIndex;
import cn.oyzh.easyshell.sqlite.schema.SQLiteSchemaManager;
import cn.oyzh.easyshell.sqlite.table.SQLiteAlertTableParam;
import cn.oyzh.easyshell.sqlite.trigger.SQLiteTrigger;
import cn.oyzh.fx.db.DBSqlGenerator;

import java.util.List;
import java.util.UUID;

/**
 * SQLite修改表SQL生成器
 *
 * <p>SQLite 不支持 MySQL 式的字段修改语法，必要差异是采用临时表重建。</p>
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class SQLiteTableAlertSqlGenerator extends DBSqlGenerator {

    /**
     * 生成SQL片段
     *
     * @param param 修改表参数
     */
    private void _generate(SQLiteAlertTableParam param) {
        String originalName = StringUtil.blankToDefault(
                param.getTable().originalName(), param.tableName());
        String tableName = param.tableName();
        String temporaryName = "__easyshell_new_" + UUID.randomUUID().toString().replace("-", "");
        StringBuilder script = new StringBuilder("BEGIN;\n");
        script.append(SQLiteTableCreateSqlGenerator.createTable(temporaryName,
                param.getColumns(), param.getTable().getConstraints(),
                param.getTable().isWithoutRowid(), param.getTable().isStrict())).append('\n');
        List<String> copyColumns = param.getColumns().stream()
                .filter(column -> !column.isCreated() && !column.isDeleted() && !column.isGenerated())
                .map(column -> StringUtil.blankToDefault(column.originalName(), column.getName()))
                .map(SQLiteGeneratorUtil::quote)
                .toList();
        if (!copyColumns.isEmpty()) {
            String columns = String.join(", ", copyColumns);
            script.append("INSERT INTO ").append(SQLiteGeneratorUtil.quote(temporaryName))
                    .append(" (").append(columns).append(")\nSELECT ")
                    .append(columns).append(" FROM ")
                    .append(SQLiteGeneratorUtil.quote(originalName)).append(";\n");
        }
        script.append("DROP TABLE ").append(SQLiteGeneratorUtil.quote(originalName)).append(";\n");
        script.append("ALTER TABLE ").append(SQLiteGeneratorUtil.quote(temporaryName))
                .append(" RENAME TO ").append(SQLiteGeneratorUtil.quote(tableName)).append(";\n");
        if (param.getIndexes() != null) {
            for (SQLiteIndex index : param.getIndexes()) {
                if (!index.isDeleted() && StringUtil.isNotBlank(index.getDefinition())) {
                    String definition = SQLiteSchemaManager.replaceTableReference(
                            index.normalizedDefinition(originalName, tableName), originalName, tableName);
                    script.append(definition).append(";\n");
                }
            }
        }
        if (param.getTriggers() != null) {
            for (SQLiteTrigger trigger : param.getTriggers()) {
                if (!trigger.isDeleted() && StringUtil.isNotBlank(trigger.getDefinition())) {
                    String definition = SQLiteSchemaManager.replaceTableReference(
                            trigger.normalizedDefinition(originalName, tableName), originalName, tableName);
                    script.append(definition).append(";\n");
                }
            }
        }
        script.append("COMMIT;");
        this.sqlBuilder.append(script);
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改表参数
     * @return SQL列表
     */
    public List<String> generate(SQLiteAlertTableParam param) {
        this._generate(param);
        return this.buildSql();
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改表参数
     * @return SQL语句
     */
    public String generateSingle(SQLiteAlertTableParam param) {
        this._generate(param);
        return this.buildSqlSingle();
    }

    /**
     * 生成SQL列表
     *
     * @param param 修改表参数
     * @return SQL列表
     */
    public static List<String> generateSql(SQLiteAlertTableParam param) {
        return new SQLiteTableAlertSqlGenerator().generate(param);
    }

    /**
     * 生成单条SQL
     *
     * @param param 修改表参数
     * @return SQL语句
     */
    public static String generateSqlSingle(SQLiteAlertTableParam param) {
        return new SQLiteTableAlertSqlGenerator().generateSingle(param);
    }
}
