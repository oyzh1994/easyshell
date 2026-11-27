package cn.oyzh.easyshell.oracle;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.oracle.index.OracleIndex;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.sql.DBSqlParser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Oracle索引定义解析测试
 * <p>
 * 索引定义以DDL形式直接编辑，这里校验定义中的索引名称解析、语句识别与拆分。
 *
 * @since 2026-11-27
 */
public class OracleIndexDefinitionTest {

    /**
     * 构造索引
     *
     * @param name      名称
     * @param tableName 表名称
     * @param type      类型
     * @param method    方式
     * @param columns   字段
     * @return 索引
     */
    private OracleIndex index(String name, String tableName, String type, String method, String... columns) {
        OracleIndex index = new OracleIndex();
        index.setSchema("DEMO");
        index.setName(name);
        index.setTableName(tableName);
        index.setType(type);
        index.setMethod(method);
        for (String column : columns) {
            index.addColumn(column);
        }
        return index;
    }

    @Test
    public void buildDefinitionFromDictionary() {
        // 普通索引
        OracleIndex normal = this.index("IDX_A", "T_A", "NORMAL", "BTREE", "C1", "C2");
        assertEquals("CREATE INDEX \"DEMO\".\"IDX_A\" ON \"DEMO\".\"T_A\" (\"C1\",\"C2\")",
                ShellOracleHelper.buildCreateIndexSql("DEMO", normal));
        // 唯一索引
        OracleIndex unique = this.index("IDX_B", "T_B", "UNIQUE", "BTREE", "C1");
        assertEquals("CREATE UNIQUE INDEX \"DEMO\".\"IDX_B\" ON \"DEMO\".\"T_B\" (\"C1\")",
                ShellOracleHelper.buildCreateIndexSql("DEMO", unique));
        // 位图索引
        OracleIndex bitmap = this.index("IDX_C", "T_C", "NORMAL", "BITMAP", "C1");
        assertEquals("CREATE BITMAP INDEX \"DEMO\".\"IDX_C\" ON \"DEMO\".\"T_C\" (\"C1\")",
                ShellOracleHelper.buildCreateIndexSql("DEMO", bitmap));
    }

    @Test
    public void parseIndexNameFromDefinition() {
        // 普通、唯一、位图索引
        assertEquals("IDX_A", ShellOracleHelper.parseIndexName(
                "CREATE INDEX \"DEMO\".\"IDX_A\" ON \"DEMO\".\"T_A\" (\"C1\")"));
        assertEquals("IDX_B", ShellOracleHelper.parseIndexName(
                "create unique index \"DEMO\".\"IDX_B\" on \"DEMO\".\"T_B\" (\"C1\")"));
        assertEquals("IDX_C", ShellOracleHelper.parseIndexName(
                "CREATE BITMAP INDEX \"IDX_C\" ON \"T_C\" (\"C1\")"));
        // 无模式前缀
        assertEquals("IDX_D", ShellOracleHelper.parseIndexName("CREATE INDEX IDX_D ON T_D (C1)"));
        // 其他数据库方言
        assertEquals("IDX_E", ShellOracleHelper.parseIndexName("CREATE INDEX IF NOT EXISTS `IDX_E` ON `t` (`c`)"));
        assertEquals("IDX_F", ShellOracleHelper.parseIndexName("CREATE NONCLUSTERED INDEX [IDX_F] ON [t] ([c])"));
        // 非创建索引语句
        assertNull(ShellOracleHelper.parseIndexName("DROP INDEX \"DEMO\".\"IDX_A\""));
        assertNull(ShellOracleHelper.parseIndexName(null));
    }

    @Test
    public void detectCreateAndDropIndexSql() {
        assertTrue(ShellOracleHelper.isCreateIndexSql("CREATE INDEX \"DEMO\".\"IDX_A\" ON \"DEMO\".\"T_A\" (\"C1\")"));
        assertTrue(ShellOracleHelper.isCreateIndexSql("  create unique index IDX_A on T_A (C1)"));
        assertFalse(ShellOracleHelper.isCreateIndexSql("DROP INDEX \"DEMO\".\"IDX_A\""));
        assertFalse(ShellOracleHelper.isCreateIndexSql(null));

        assertTrue(ShellOracleHelper.isDropIndexSql("DROP INDEX \"DEMO\".\"IDX_A\""));
        assertTrue(ShellOracleHelper.isDropIndexSql("  drop index IDX_A"));
        assertFalse(ShellOracleHelper.isDropIndexSql("CREATE INDEX IDX_A ON T_A (C1)"));
        assertFalse(ShellOracleHelper.isDropIndexSql(null));
    }

    @Test
    public void definitionIsSplitIntoStatements() {
        // 注释模板不应产生可执行语句
        String template = "-- 请输入索引定义，例如：\n"
                + "-- CREATE INDEX \"DEMO\".\"IDX_A\" ON \"DEMO\".\"T_A\" (\"C1\")";
        assertTrue(DBSqlParser.getParser(template, DBDialect.ORACLE).parseSql()
                .stream().filter(StringUtil::isNotBlank).toList().isEmpty());
        // 删除 + 创建语句会被拆分为两条
        String definition = "DROP INDEX \"DEMO\".\"IDX_A\";\n"
                + "CREATE INDEX \"DEMO\".\"IDX_A\" ON \"DEMO\".\"T_A\" (\"C1\",\"C2\");";
        List<String> sqlList = DBSqlParser.getParser(definition, DBDialect.ORACLE).parseSql()
                .stream().filter(StringUtil::isNotBlank).toList();
        assertEquals(2, sqlList.size());
        assertTrue(ShellOracleHelper.isDropIndexSql(sqlList.get(0)));
        assertTrue(ShellOracleHelper.isCreateIndexSql(sqlList.get(1)));
        assertEquals("IDX_A", ShellOracleHelper.parseIndexName(sqlList.get(1)));
    }

    @Test
    public void indexCompareUsesName() {
        OracleIndex index1 = this.index("IDX_H", "T_H", "NORMAL", "BTREE", "C1");
        OracleIndex index2 = this.index("idx_h", "T_H", "NORMAL", "BTREE", "C1");
        assertTrue(index1.compare(index2));
        assertTrue(index2.compare(index1));
        assertFalse(index1.compare(this.index("IDX_I", "T_H", "NORMAL", "BTREE", "C1")));
        assertFalse(index1.compare(null));
    }
}
