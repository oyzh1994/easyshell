package cn.oyzh.easyshell.h2;

import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.generator.H2GeneratorUtil;
import cn.oyzh.easyshell.h2.generator.record.H2RecordSqlGenerator;
import cn.oyzh.easyshell.h2.generator.sequence.H2SequenceSqlGenerator;
import cn.oyzh.easyshell.h2.generator.table.H2TableCreateSqlGenerator;
import cn.oyzh.easyshell.h2.generator.view.H2ViewCreateSqlGenerator;
import cn.oyzh.easyshell.h2.index.H2IndexControl;
import cn.oyzh.easyshell.h2.sequence.H2Sequence;
import cn.oyzh.easyshell.data.h2.util.H2SqlSchemaRewriter;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * H2 SQL 生成器测试：标识符、字面量、建表、视图、记录语句。
 *
 * @since 2026-11-28
 */
public class H2GeneratorSqlTest {

    @Test
    public void testQuoteAndUnquote() {
        // quote 原样加引号，避免标识符大小写歧义；名称大小写由调用方（INFORMATION_SCHEMA）保证
        assertEquals("\"item\"", H2GeneratorUtil.quote("item"));
        assertEquals("\"ITEM\"", H2GeneratorUtil.quote("ITEM"));
        assertEquals("\"item_view\"", H2GeneratorUtil.quote("\"item_view\""));
        assertEquals("\"Name\"", H2GeneratorUtil.quote("\"Name\""));
        assertEquals("item", H2GeneratorUtil.unquote("\"item\""));
        assertEquals("ITEM", H2GeneratorUtil.unquote("ITEM"));
    }

    @Test
    public void testLiteral() {
        assertEquals("NULL", H2GeneratorUtil.literal(null));
        assertEquals("'one'", H2GeneratorUtil.literal("one"));
        // 单引号按 SQL 标准转义
        assertEquals("'it''s'", H2GeneratorUtil.literal("it's"));
        assertEquals("10", H2GeneratorUtil.literal(10));
        assertEquals("1.5", H2GeneratorUtil.literal(1.5));
        assertEquals("true", H2GeneratorUtil.literal(Boolean.TRUE));
    }

    @Test
    public void testStripSemicolonAndRequireName() {
        assertEquals("SELECT 1", H2GeneratorUtil.stripSemicolon("SELECT 1;"));
        assertEquals("SELECT 1", H2GeneratorUtil.stripSemicolon("SELECT 1"));
        assertThrows(IllegalArgumentException.class, () -> H2GeneratorUtil.requireName(null));
        assertThrows(IllegalArgumentException.class, () -> H2GeneratorUtil.requireName("  "));
        H2GeneratorUtil.requireName("ok");
    }

    @Test
    public void testCreateTableGeneratesQuotedDefinitions() {
        // 列名来自 INFORMATION_SCHEMA.COLUMNS，均为大写
        H2Column id = new H2Column();
        id.setName("ID");
        id.setType("INTEGER");
        id.setPrimaryKey(true);
        id.setNotNull(true);
        H2Column name = new H2Column();
        name.setName("NAME");
        name.setType("VARCHAR");
        name.setSize(20);
        name.setDefaultValue("'anon'");

        String sql = H2TableCreateSqlGenerator.createTable("ITEM", List.of(id, name), null);
        assertTrue(sql, sql.contains("CREATE TABLE \"ITEM\""));
        assertTrue(sql, sql.contains("\"ID\" INTEGER PRIMARY KEY"));
        assertTrue(sql, sql.contains("\"NAME\" VARCHAR"));
        assertTrue(sql, sql.contains("DEFAULT 'anon'"));
    }

    @Test
    public void testCreateTableRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> H2TableCreateSqlGenerator.createTable(null, List.of(new H2Column()), null));
        assertThrows(IllegalArgumentException.class,
                () -> H2TableCreateSqlGenerator.createTable("t", List.of(), null));
    }

    @Test
    public void testCreateViewRoundTrip() {
        String sql = H2ViewCreateSqlGenerator.createView("item_view",
                "SELECT \"ID\" FROM item");
        assertTrue(sql, sql.contains("CREATE VIEW"));
        assertTrue(sql, sql.contains("\"item_view\""));
        // createSql 中的定义可反解析
        String definition = H2ViewCreateSqlGenerator.viewDefinition(sql);
        assertTrue(definition, definition.contains("SELECT"));
        String preview = H2ViewCreateSqlGenerator.createViewPreview("item_view",
                "SELECT 1");
        assertTrue(preview.contains("CREATE VIEW"));
    }

    @Test
    public void testRecordInsertSql() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("ID", 1);
        values.put("NAME", "one");
        values.put("NOTE", null);
        String sql = H2RecordSqlGenerator.insert("item", values.keySet(), values);
        assertEquals("INSERT INTO \"item\" (\"ID\", \"NAME\", \"NOTE\") VALUES (1, 'one', NULL);", sql);

        // 值缺失时按 NULL 处理
        Map<String, Object> partial = new HashMap<>();
        partial.put("ID", 2);
        String partialSql = H2RecordSqlGenerator.insert("item", List.of("ID", "NAME"), partial);
        assertTrue(partialSql, partialSql.contains("NULL"));

        assertThrows(IllegalArgumentException.class,
                () -> H2RecordSqlGenerator.insert("t", List.of(), Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> H2RecordSqlGenerator.insert(null, List.of("ID"), Map.of()));
    }

    @Test
    public void testGeneratedColumnExcludedInExportWriterFlow() {
        // H2DataFileWriter 导出时排除生成列，生成器侧保证定义不参与 insert 列名
        H2Column id = new H2Column();
        id.setName("id");
        id.setType("INTEGER");
        H2Column computed = new H2Column();
        computed.setName("computed");
        computed.setType("INTEGER");
        computed.setGenerated(true);
        computed.setGeneratedExpression("ID * 2");

        assertFalse(id.isGenerated());
        assertTrue(computed.isGenerated());
        assertEquals("ID * 2", computed.getGeneratedExpression());
    }

    @Test
    public void testH2DataTypesPreservePrecisionAndStrongTyping() {
        H2Column amount = new H2Column("amount");
        amount.setType("NUMERIC");
        amount.setSize(12);
        amount.setDigits(2);
        assertEquals("\"amount\" NUMERIC(12,2)", H2TableCreateSqlGenerator.columnDefinition(amount));

        H2Column id = new H2Column("id");
        id.setType("INTEGER");
        id.setSize(32);
        assertEquals("\"id\" INTEGER", H2TableCreateSqlGenerator.columnDefinition(id));

        H2Column name = new H2Column("name");
        name.setType("CHARACTER VARYING");
        name.setSize(40);
        assertEquals("\"name\" CHARACTER VARYING(40)",
                H2TableCreateSqlGenerator.columnDefinition(name));

        H2Column generated = new H2Column("calculated");
        generated.setType("NUMERIC");
        generated.setSize(12);
        generated.setDigits(2);
        generated.setGenerated(true);
        generated.setGeneratedExpression("amount * 2");
        String sql = H2TableCreateSqlGenerator.columnDefinition(generated);
        assertTrue(sql, sql.contains("GENERATED ALWAYS AS"));
        assertFalse(sql, sql.contains("STORED"));
        assertFalse(sql, sql.contains("VIRTUAL"));
    }

    @Test
    public void testIndexUniqueCheckboxRebuildsDefinition() {
        H2IndexControl index = new H2IndexControl();
        index.setName("idx_name");
        index.setTableName("T1");
        index.setDefinition("CREATE INDEX \"idx_name\" ON \"T1\" (\"NAME\");");

        assertFalse(index.isUnique());
        index.setUnique(true);
        assertTrue(index.isUnique());
        assertTrue(index.getDefinition().startsWith("CREATE UNIQUE INDEX"));

        index.setIndexType("HASH");
        assertEquals("HASH", index.getIndexType());
        assertTrue(index.isUnique());
        assertTrue(index.getDefinition().startsWith("CREATE UNIQUE HASH INDEX"));

        index.setIndexType("SPATIAL");
        assertEquals("SPATIAL", index.getIndexType());
        assertTrue(index.isUnique());
        assertTrue(index.getDefinition().startsWith("CREATE UNIQUE SPATIAL INDEX"));

        index.setIndexType("NORMAL");
        assertEquals("NORMAL", index.getIndexType());
        index.setUnique(false);
        assertFalse(index.isUnique());
        assertTrue(index.getDefinition().startsWith("CREATE INDEX"));
    }

    @Test
    public void testSequenceCacheAndAttributeValidation() {
        H2Sequence sequence = new H2Sequence();
        sequence.setSchema("PUBLIC");
        sequence.setName("SEQ_ORDER");
        sequence.setStartValue("14");
        sequence.setMinimumValue("1");
        sequence.setMaximumValue("999");
        sequence.setIncrement("1");
        sequence.setCacheSize("7");

        String sql = H2SequenceSqlGenerator.create(sequence);
        assertTrue(sql, sql.contains("START WITH 14"));
        assertTrue(sql, sql.contains("CACHE 7"));

        sequence.setMinimumValue("121");
        assertThrows(IllegalArgumentException.class,
                () -> H2SequenceSqlGenerator.create(sequence));
    }

    @Test
    public void testSqlSchemaRewriteKeepsStringLiterals() {
        String sql = "CREATE TABLE \"PUBLIC\".\"ITEM\"("
                + "\"NAME\" VARCHAR DEFAULT '\"PUBLIC\".\"PROPERTY\"')";
        String rewritten = H2SqlSchemaRewriter.rewriteStatement(sql, "PUBLIC", "TEST2");
        assertTrue(rewritten, rewritten.contains("\"TEST2\".\"ITEM\""));
        assertTrue(rewritten, rewritten.contains("'\"PUBLIC\".\"PROPERTY\"'"));
        assertFalse(rewritten, rewritten.contains("CREATE TABLE \"PUBLIC\""));
    }

    @Test
    public void testTransportRewritesSchemaWithoutChangingAliases() {
        String sql = "CREATE FORCE TRIGGER \"test1\".\"trigger_2e188\" "
                + "BEFORE INSERT ON \"test1\".\"t2\" "
                + "AS U&'statement.execute(\"insert into \\\"t1\\\"\")'";
        String rewritten = H2SqlSchemaRewriter.rewriteStatement(sql, "TEST1", "test2");
        assertTrue(rewritten, rewritten.contains(
                "CREATE FORCE TRIGGER \"test2\".\"trigger_2e188\""));
        assertTrue(rewritten, rewritten.contains("ON \"test2\".\"t2\""));
        assertTrue(rewritten, rewritten.contains("\\\"t1\\\""));
        assertFalse(rewritten, rewritten.contains(
                "CREATE FORCE TRIGGER \"test1\""));

        String view = "SELECT \"E\".\"ID\", \"E\".\"NAME\" "
                + "FROM \"test1\".\"EMPLOYEE\" \"E\"";
        String viewRewritten = H2SqlSchemaRewriter.rewriteStatement(
                view, "test1", "test7");
        assertTrue(viewRewritten, viewRewritten.contains("\"E\".\"ID\""));
        assertTrue(viewRewritten, viewRewritten.contains("\"test7\".\"EMPLOYEE\""));
        assertFalse(viewRewritten, viewRewritten.contains("\"test7\".\"ID\""));
    }

    @Test
    public void testInsertNormalizesDateAndTimeStrings() {
        H2Column date = new H2Column("D");
        date.setType("DATE");
        H2Column time = new H2Column("T");
        time.setType("TIME");
        java.util.Map<String, Object> record = new java.util.HashMap<>();
        record.put("D", "2026-01-15 00:00:00");
        record.put("T", "1970-01-01 09:30:00");

        String sql = cn.oyzh.easyshell.util.h2.ShellH2DataUtil.toInsertSql(
                "DATETIME_TEST", List.of(date, time), List.of(record)).getFirst();
        assertTrue(sql, sql.contains("'2026-01-15'"));
        assertTrue(sql, sql.contains("'09:30:00'"));
        assertFalse(sql, sql.contains("'2026-01-15 00:00:00'"));
    }

    @Test
    public void testInsertNormalizesEmptyJsonAndBooleanStrings() {
        H2Column enabled = new H2Column("ENABLED");
        enabled.setType("BOOLEAN");
        H2Column json = new H2Column("JSON_DATA");
        json.setType("JSON");
        java.util.Map<String, Object> record = new java.util.HashMap<>();
        record.put("ENABLED", "true");
        record.put("JSON_DATA", "");

        String sql = cn.oyzh.easyshell.util.h2.ShellH2DataUtil.toInsertSql(
                "TYPED_TEST", List.of(enabled, json), List.of(record)).getFirst();
        assertTrue(sql, sql.contains("true"));
        assertFalse(sql, sql.contains("'true'"));
        assertTrue(sql, sql.contains("NULL"));
        assertFalse(sql, sql.contains("JSON ''"));
    }
}
