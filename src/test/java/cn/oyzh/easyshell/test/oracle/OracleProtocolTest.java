package cn.oyzh.easyshell.test.oracle;

import cn.oyzh.easyshell.data.oracle.handler.ShellOracleDataRunSqlFileHandler;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.oracle.ShellOracleConnManager;
import cn.oyzh.easyshell.oracle.ShellOracleHelper;
import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.column.OracleColumns;
import cn.oyzh.easyshell.oracle.generator.sequence.OracleSequenceCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.table.OracleTableAlertSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.table.OracleTableCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.generator.view.OracleViewCreateSqlGenerator;
import cn.oyzh.easyshell.oracle.sequence.OracleCreateSequenceParam;
import cn.oyzh.easyshell.oracle.sequence.OracleSequence;
import cn.oyzh.easyshell.oracle.record.OracleRecord;
import cn.oyzh.easyshell.oracle.table.OracleAlertTableParam;
import cn.oyzh.easyshell.oracle.table.OracleCreateTableParam;
import cn.oyzh.easyshell.oracle.table.OracleTable;
import cn.oyzh.easyshell.oracle.view.OracleCreateViewParam;
import cn.oyzh.easyshell.oracle.view.OracleView;
import cn.oyzh.easyshell.util.oracle.ShellOracleColumnUtil;
import cn.oyzh.easyshell.util.oracle.ShellOracleDataUtil;
import cn.oyzh.fx.db.DBConnConfig;
import cn.oyzh.fx.db.DBColumnFieldManager;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBRecordData;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Oracle协议连接与DDL兼容性测试。
 */
public class OracleProtocolTest {

    @BeforeClass
    public static void initColumnFields() {
        ShellOracleColumnUtil.init();
    }

    @Test
    public void buildsServiceNameSidDescriptorAndSslUrls() {
        DBConnConfig config = new DBConnConfig();
        config.setHost("db.example.com");
        config.setPort(1521);
        config.setDatabase("FREEPDB1");

        assertEquals("jdbc:oracle:thin:@tcp://db.example.com:1521/FREEPDB1",
                ShellOracleConnManager.buildConnectionString(config));

        config.setDatabase("SID:ORCL");
        assertEquals("jdbc:oracle:thin:@db.example.com:1521:ORCL",
                ShellOracleConnManager.buildConnectionString(config));

        config.setDatabase("(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=db)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=ORCL)))");
        assertEquals("jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=db)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=ORCL)))",
                ShellOracleConnManager.buildConnectionString(config));

        config.setDatabase("FREEPDB1");
        config.setUseSSL(true);
        assertEquals("jdbc:oracle:thin:@tcps://db.example.com:1521/FREEPDB1",
                ShellOracleConnManager.buildConnectionString(config));
    }

    @Test
    public void matchesOracleTypesWithPrecisionAndScale() {
        assertTrue(DBColumnFieldManager.supportTimestamp(DBDialect.ORACLE, "TIMESTAMP(6)"));
        assertTrue(DBColumnFieldManager.supportTimestamp(DBDialect.ORACLE, "TIMESTAMP(6) WITH TIME ZONE"));
        assertTrue(DBColumnFieldManager.supportString(DBDialect.ORACLE, "VARCHAR2(100)"));
        assertTrue(DBColumnFieldManager.supportDigits(DBDialect.ORACLE, "NUMBER(10,2)"));
        assertTrue(DBColumnFieldManager.supportDigits(DBDialect.ORACLE, "FLOAT"));
        assertTrue(DBColumnFieldManager.supportDigits(DBDialect.ORACLE, "DOUBLE PRECISION"));
        assertTrue(DBColumnFieldManager.supportDigits(DBDialect.ORACLE, "BINARY_FLOAT"));
        assertTrue(DBColumnFieldManager.supportDigits(DBDialect.ORACLE, "BINARY_DOUBLE"));
        assertFalse(DBColumnFieldManager.supportString(DBDialect.ORACLE, "BINARY_FLOAT"));
        assertFalse(DBColumnFieldManager.supportString(DBDialect.ORACLE, "BINARY_DOUBLE"));
        assertNull(DBColumnFieldManager.exampleValue(DBDialect.ORACLE, "TIMESTAMP(6)"));
    }

    @Test
    public void ignoresOracleTimestampDefaultExpressions() {
        OracleColumn createdAt = column("CREATED_AT", "TIMESTAMP(6)", "DEMO", "USERS");
        OracleRecord record = new OracleRecord(new OracleColumns(List.of(createdAt)));
        record.putValue(createdAt, "SYSTIMESTAMP ");

        DBRecordData data = record.getRecordData();
        assertFalse(data.columns().contains("CREATED_AT"));
    }

    @Test
    public void excludesOracleVirtualColumnsFromDml() {
        OracleColumn username = column("USERNAME", "VARCHAR2", "DEMO", "ALL_TYPE_TEST");
        OracleColumn virtual = column("C_VIRTUAL", "VARCHAR2", "DEMO", "ALL_TYPE_TEST");
        virtual.setVirtual(true);
        OracleColumn bfile = column("C_BFILE", "BFILE", "DEMO", "ALL_TYPE_TEST");
        OracleColumn xml = column("C_XML", "XMLTYPE", "DEMO", "ALL_TYPE_TEST");
        // Oracle 11g在ALL_TAB_COLS中把XMLTYPE标记为虚拟列，但它仍然允许写入
        xml.setVirtual(true);

        OracleColumns columns = new OracleColumns(List.of(username, virtual, bfile, xml));
        OracleRecord record = new OracleRecord(columns);
        record.putValue(username, "alice");
        record.putValue(virtual, "computed");
        record.putValue(bfile, "external.dat");
        record.putValue(xml, "<root/>");

        DBRecordData data = record.getRecordData();
        assertFalse(data.columns().contains("C_VIRTUAL"));
        assertFalse(data.columns().contains("C_BFILE"));
        assertTrue(data.columns().contains("C_XML"));

        String sql = ShellOracleDataUtil.toInsertSql(columns, record, true);
        assertFalse(sql.contains("\"C_VIRTUAL\""));
        assertFalse(sql.contains("\"C_BFILE\""));
        assertTrue(sql.contains("\"USERNAME\""));
        assertTrue(sql.contains("\"C_XML\""));
    }

    @Test
    public void renamesOracleRoutineDefinitionsWithoutChangingBody() {
        String procedureDefinition = """
                CREATE OR REPLACE PROCEDURE      ADD_USER(
                    p_username IN VARCHAR2
                ) AS
                BEGIN
                    ADD_USER(p_username);
                END;
                /
                """;
        String expectedDefinition = """
                CREATE OR REPLACE PROCEDURE      "ADD_USER_RENAMED"(
                    p_username IN VARCHAR2
                ) AS
                BEGIN
                    ADD_USER(p_username);
                END;
                """;
        assertEquals(expectedDefinition.stripTrailing(), ShellOracleHelper.renameRoutineDefinition(
                procedureDefinition, "PROCEDURE", "ADD_USER", "ADD_USER_RENAMED"));

        String qualifiedDefinition = "CREATE OR REPLACE PROCEDURE \"DEMO\".\"ADD_USER\" AS BEGIN NULL; END;";
        assertEquals("CREATE OR REPLACE PROCEDURE \"DEMO\".\"ADD_USER_RENAMED\" AS BEGIN NULL; END;",
                ShellOracleHelper.renameRoutineDefinition(
                        qualifiedDefinition, "PROCEDURE", "ADD_USER", "ADD_USER_RENAMED"));

        String functionDefinition = "CREATE OR REPLACE NONEDITIONABLE FUNCTION DEMO . USER_COUNT RETURN NUMBER AS BEGIN RETURN 0; END; /";
        assertEquals("CREATE OR REPLACE NONEDITIONABLE FUNCTION DEMO . \"USER_COUNT_RENAMED\" RETURN NUMBER AS BEGIN RETURN 0; END;",
                ShellOracleHelper.renameRoutineDefinition(
                        functionDefinition, "FUNCTION", "USER_COUNT", "USER_COUNT_RENAMED"));

        try {
            ShellOracleHelper.renameRoutineDefinition(
                    "CREATE OR REPLACE PROCEDURE ADD_USER AS BEGIN NULL; END;",
                    "PROCEDURE", "MISSING_PROCEDURE", "NEW_PROCEDURE");
            fail("Expected missing routine name to be rejected");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("MISSING_PROCEDURE"));
        }
    }

    @Test
    public void generatesSequenceTriggerCompatibleAutoIncrement() {
        OracleTable table = new OracleTable();
        table.setSchema("APP");
        table.setName("USERS");

        OracleColumn id = column("ID", "NUMBER", "APP", "USERS");
        id.setSize(19);
        id.setPrimaryKey(true);
        id.setNullable(false);
        id.setAutoIncrement(true);

        OracleColumn name = column("NAME", "VARCHAR2", "APP", "USERS");
        name.setSize(255);
        name.setNullable(false);

        OracleCreateTableParam param = new OracleCreateTableParam();
        param.setTable(table);
        param.setColumns(new OracleColumns(List.of(id, name)));
        param.setTriggers(new DBObjects<>());

        List<String> sql = OracleTableCreateSqlGenerator.generateSql(param);
        String ddl = String.join("\n", sql);
        assertTrue(ddl.contains("CREATE TABLE \"APP\".\"USERS\""));
        assertTrue(ddl.contains("PRIMARY KEY (\"ID\")"));
        assertTrue(sql.contains("CREATE SEQUENCE \"APP\".\"USERS_ID_SEQ\" START WITH 1 INCREMENT BY 1 NOCACHE;"));
        assertTrue(ddl.contains("CREATE OR REPLACE TRIGGER \"APP\".\"USERS_ID_TRG\" BEFORE INSERT ON \"APP\".\"USERS\""));
        assertTrue(ddl.contains(":NEW.\"ID\""));
    }

    @Test
    public void generatesOracleIdentityWhenExplicitlySelected() {
        OracleTable table = new OracleTable();
        table.setSchema("APP");
        table.setName("USERS");

        OracleColumn id = column("ID", "NUMBER", "APP", "USERS");
        id.setSize(19);
        id.setPrimaryKey(true);
        id.setNullable(false);
        id.setAutoIncrement(true);
        id.setAutoIncrementType("IDENTITY");

        OracleCreateTableParam param = new OracleCreateTableParam();
        param.setTable(table);
        param.setColumns(new OracleColumns(List.of(id)));
        param.setTriggers(new DBObjects<>());

        String ddl = String.join("\n", OracleTableCreateSqlGenerator.generateSql(param));
        assertTrue("Unexpected Oracle identity DDL: " + ddl,
                ddl.contains("\"ID\" NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY"));
        assertFalse(ddl.contains("CREATE SEQUENCE"));
        assertFalse(ddl.contains("CREATE OR REPLACE TRIGGER"));
    }

    @Test
    public void defaultsMissingSizesAndFallsBackToSequenceOnOracle11g() {
        OracleTable table = new OracleTable();
        table.setSchema("DEMO");
        table.setName("t1");

        OracleColumn text = column("a", "VARCHAR", "DEMO", "t1");
        text.setNullable(true);

        OracleColumn id = column("id", "INT", "DEMO", "t1");
        id.setPrimaryKey(true);
        id.setNullable(false);
        id.setAutoIncrement(true);
        id.setAutoIncrementType("IDENTITY");

        OracleCreateTableParam param = new OracleCreateTableParam();
        param.setTable(table);
        param.setColumns(new OracleColumns(List.of(text, id)));
        param.setTriggers(new DBObjects<>());

        String modernDdl = String.join("\n", OracleTableCreateSqlGenerator.generateSql(param, true));
        assertTrue("Missing VARCHAR length should default to 255: " + modernDdl,
                modernDdl.contains("\"a\" VARCHAR(255) NULL"));
        assertTrue(modernDdl.contains("\"id\" INT GENERATED BY DEFAULT ON NULL AS IDENTITY"));

        String legacyDdl = String.join("\n", OracleTableCreateSqlGenerator.generateSql(param, false));
        assertTrue("Oracle 11g should receive a default VARCHAR length: " + legacyDdl,
                legacyDdl.contains("\"a\" VARCHAR(255) NULL"));
        assertFalse(legacyDdl.contains("GENERATED BY DEFAULT ON NULL AS IDENTITY"));
        assertTrue(legacyDdl.contains("CREATE SEQUENCE \"DEMO\".\"T1_ID_SEQ\""));
        assertTrue(legacyDdl.contains("CREATE OR REPLACE TRIGGER \"DEMO\".\"T1_ID_TRG\""));
    }

    @Test
    public void renamesLegacyAutoIncrementObjects() {
        OracleTable table = new OracleTable();
        table.setSchema("APP");
        table.setName("USERS");

        OracleColumn id = column("ID", "NUMBER", "APP", "USERS");
        id.setSize(19);
        id.setPrimaryKey(true);
        id.setAutoIncrement(true);
        id.setName("USER_ID");

        OracleAlertTableParam param = new OracleAlertTableParam();
        param.setTable(table);
        param.setColumns(new OracleColumns(List.of(id)));
        param.setTriggers(new DBObjects<>());

        String ddl = OracleTableAlertSqlGenerator.generateSqlSingle(param);
        assertTrue(ddl.contains("RENAME COLUMN \"ID\" TO \"USER_ID\""));
        assertTrue(ddl.contains("DROP TRIGGER \"APP\".\"USERS_ID_TRG\""));
        assertTrue(ddl.contains("DROP SEQUENCE \"APP\".\"USERS_ID_SEQ\""));
        assertTrue(ddl.contains("CREATE SEQUENCE \"APP\".\"USERS_USER_ID_SEQ\""));
        assertTrue(ddl.contains("CREATE OR REPLACE TRIGGER \"APP\".\"USERS_USER_ID_TRG\""));
    }

    @Test
    public void skipsColumnModifyWhenOnlyCommentChanges() {
        OracleTable table = new OracleTable();
        table.setSchema("DEMO");
        table.setName("USERS");

        OracleColumn username = column("USERNAME", "VARCHAR2", "DEMO", "USERS");
        username.setSize(100);
        username.setNullable(false);
        username.setComment("old comment");
        username.setComment("new comment");

        OracleAlertTableParam param = new OracleAlertTableParam();
        param.setTable(table);
        param.setColumns(new OracleColumns(List.of(username)));

        String sql = OracleTableAlertSqlGenerator.generateSqlSingle(param);
        assertFalse(sql.contains(" MODIFY ("));
        assertTrue(sql.contains("COMMENT ON COLUMN"));
    }

    @Test
    public void omitsUnchangedNotNullInColumnModify() {
        OracleTable table = new OracleTable();
        table.setSchema("DEMO");
        table.setName("USERS");

        OracleColumn username = column("USERNAME", "VARCHAR2", "DEMO", "USERS");
        username.setSize(100);
        username.setNullable(false);
        username.setSize(128);

        OracleAlertTableParam param = new OracleAlertTableParam();
        param.setTable(table);
        param.setColumns(new OracleColumns(List.of(username)));

        String sql = OracleTableAlertSqlGenerator.generateSqlSingle(param);
        assertTrue(sql.contains(" MODIFY ("));
        assertFalse(sql.contains("NOT NULL"));
    }

    @Test
    public void preservesCustomSequenceAndTriggerNames() {
        OracleTable table = new OracleTable();
        table.setSchema("APP");
        table.setName("USERS");

        OracleColumn id = column("ID", "NUMBER", "APP", "USERS");
        id.setSize(19);
        id.setPrimaryKey(true);
        id.setNullable(false);
        id.setAutoIncrement(true);
        id.setAutoIncrementType("SEQUENCE_TRIGGER");
        id.setAutoIncrementSequenceName("CUSTOM_ID_SEQ");
        id.setAutoIncrementTriggerName("CUSTOM_ID_TRG");
        id.setName("USER_ID");

        OracleAlertTableParam param = new OracleAlertTableParam();
        param.setTable(table);
        param.setColumns(new OracleColumns(List.of(id)));
        param.setTriggers(new DBObjects<>());

        String ddl = OracleTableAlertSqlGenerator.generateSqlSingle(param);
        assertTrue(ddl.contains("RENAME COLUMN \"ID\" TO \"USER_ID\""));
        assertFalse(ddl.contains("DROP TRIGGER"));
        assertFalse(ddl.contains("DROP SEQUENCE"));
        assertFalse(ddl.contains("CREATE SEQUENCE"));
        assertTrue(ddl.contains("CREATE OR REPLACE TRIGGER \"APP\".\"CUSTOM_ID_TRG\""));
        assertTrue(ddl.contains("SELECT \"APP\".\"CUSTOM_ID_SEQ\".NEXTVAL INTO :NEW.\"USER_ID\""));
    }

    @Test
    public void generatesCompatibleSequenceAndViewDdl() {
        OracleSequence sequence = new OracleSequence("USER_ID_SEQ");
        sequence.setSchema("APP");
        sequence.setIncrementBy(BigInteger.ONE);
        sequence.setMinVal(BigInteger.ONE);
        sequence.setCurrentVal(BigInteger.ONE);

        OracleCreateSequenceParam sequenceParam = new OracleCreateSequenceParam();
        sequenceParam.setSequence(sequence);
        String sequenceDdl = OracleSequenceCreateSqlGenerator.generateSqlSingle(sequenceParam);
        assertTrue(sequenceDdl.startsWith("CREATE SEQUENCE \"APP\".\"USER_ID_SEQ\""));
        assertTrue(sequenceDdl.contains(" START WITH 1 "));
        assertTrue(sequenceDdl.endsWith("NOCYCLE"));

        OracleView view = new OracleView();
        view.setSchema("APP");
        view.setName("ACTIVE_USERS");
        view.setDefinition("SELECT * FROM \"APP\".\"USERS\" WHERE \"ACTIVE\" = 1\nWITH READ ONLY");
        view.setSecurityType("CURRENT_USER");
        view.setUpdatable(false);

        OracleCreateViewParam viewParam = new OracleCreateViewParam();
        viewParam.setView(view);
        String viewDdl = OracleViewCreateSqlGenerator.generateSqlSingle(viewParam);
        assertTrue(viewDdl.contains("CREATE VIEW \"APP\".\"ACTIVE_USERS\"\nBEQUEATH CURRENT_USER AS"));
        assertTrue(viewDdl.endsWith("WITH READ ONLY;"));
        assertEquals(viewDdl.indexOf("WITH READ ONLY"), viewDdl.lastIndexOf("WITH READ ONLY"));
        assertFalse(OracleViewCreateSqlGenerator.generateSql(viewParam, false).get(0)
                .contains("BEQUEATH CURRENT_USER"));
    }

    @Test
    public void normalizesFullAndBareTriggerBodies() {
        String fullDdl = "CREATE OR REPLACE TRIGGER \"APP\".\"T\" BEFORE INSERT ON \"APP\".\"USERS\" FOR EACH ROW BEGIN :NEW.\"ID\" := 1; END;";
        assertEquals("BEGIN :NEW.\"ID\" := 1; END;", ShellOracleHelper.normalizeTriggerBody(fullDdl));
        assertEquals("BEGIN NULL; END;", ShellOracleHelper.normalizeTriggerBody("NULL"));
        assertEquals("BEGIN SELECT 1 INTO :NEW.\"ID\" FROM DUAL; END;",
                ShellOracleHelper.normalizeTriggerBody("BEGIN SELECT 1 INTO :NEW.\"ID\" FROM DUAL; END;"));
    }

    @Test
    public void normalizesTriggerWhenClauseWithParentheses() {
        String trigger = """
                CREATE OR REPLACE TRIGGER "APP"."USERS_ID_TRG"
                BEFORE INSERT ON "APP"."USERS"
                FOR EACH ROW WHEN NEW."ID" IS NULL
                BEGIN
                    SELECT USERS_ID_SEQ.NEXTVAL INTO :NEW."ID" FROM DUAL;
                END;
                """;
        String normalized = ShellOracleHelper.normalizeTriggerDefinition(trigger);
        assertTrue(normalized.contains("WHEN (NEW.\"ID\" IS NULL)"));

        String parenthesized = trigger.replace("WHEN NEW.\"ID\" IS NULL", "WHEN (NEW.\"ID\" IS NULL)");
        assertEquals(parenthesized, ShellOracleHelper.normalizeTriggerDefinition(parenthesized));

        String metadataDefinition = trigger + "ALTER TRIGGER \"APP\".\"USERS_ID_TRG\" ENABLE";
        String normalizedMetadata = ShellOracleHelper.normalizeTriggerDefinition(metadataDefinition);
        assertTrue(normalizedMetadata.contains("WHEN (NEW.\"ID\" IS NULL)"));
        assertFalse(normalizedMetadata.contains("ALTER TRIGGER"));
    }

    @Test
    public void buildsRepeatableOracleDropBlocks() {
        String ddl = ShellOracleHelper.buildDropIfExistsSql("TABLE", "USERS");
        assertTrue(ddl.contains("EXECUTE IMMEDIATE 'DROP TABLE \"USERS\"'"));
        assertTrue(ddl.contains("SQLCODE NOT IN (-942, -2289, -4043, -4080, -32794)"));
        assertTrue(ddl.strip().endsWith("/"));

        String cascadeDdl = ShellOracleHelper.buildDropIfExistsSql("TABLE", "USERS", "CASCADE CONSTRAINTS");
        assertTrue(cascadeDdl.contains("EXECUTE IMMEDIATE 'DROP TABLE \"USERS\" CASCADE CONSTRAINTS'"));
    }

    @Test
    public void detectsOracleObjectNotFoundErrors() {
        assertTrue(ShellOracleHelper.isObjectNotExists(
                new ShellException(new SQLException("table or view does not exist", "42000", 942))));
        assertTrue(ShellOracleHelper.isObjectNotExists(
                new SQLException("sequence does not exist", "42000", 2289)));
        assertTrue(ShellOracleHelper.isObjectNotExists(
                new RuntimeException("ORA-04080: trigger 'MISSING' does not exist")));
        assertFalse(ShellOracleHelper.isObjectNotExists(
                new SQLException("unique constraint violated", "23000", 1)));
        assertFalse(ShellOracleHelper.isObjectNotExists(
                new RuntimeException("ORA-00001: unique constraint violated")));
        assertTrue(ShellOracleHelper.isTablespaceUnavailable(
                new ShellException(new SQLException("tablespace unavailable", "42000", 1129))));
        assertFalse(ShellOracleHelper.isTablespaceUnavailable(
                new SQLException("table or view does not exist", "42000", 942)));
        assertTrue(ShellOracleHelper.isIgnorableDropError(
                "DROP TABLE \"USERS\"", new SQLException("missing", "42000", 942)));
        assertTrue(ShellOracleHelper.isIgnorableDropError(
                "/* script */\n-- table\nDROP TABLE \"USERS\"", new SQLException("missing", "42000", 942)));
        assertFalse(ShellOracleHelper.isIgnorableDropError(
                "CREATE TABLE \"USERS\" (\"ID\" NUMBER)", new SQLException("missing", "42000", 942)));
        assertEquals("DROP TABLE \"USERS\"",
                ShellOracleHelper.stripLeadingComments("/* script */\n-- table\nDROP TABLE \"USERS\""));
    }

    @Test
    public void splitsOracleSqlPlusAndPlSqlScripts() {
        String script = """
                CREATE OR REPLACE TYPE APP.T_VALUE AS OBJECT (
                    ID NUMBER,
                    LABEL VARCHAR2(100)
                );
                /
                CREATE OR REPLACE PROCEDURE APP.P_TEST AS
                BEGIN
                    INSERT INTO APP.LOGS (MESSAGE) VALUES (q'[a;b]');
                END;
                /
                SELECT q'{c;d}' FROM DUAL;
                """;

        List<String> statements = ShellOracleDataRunSqlFileHandler.splitOracleSql(script);
        assertEquals(3, statements.size());
        assertTrue(statements.get(0).startsWith("CREATE OR REPLACE TYPE"));
        assertFalse(statements.get(0).contains("\n/"));
        assertTrue(statements.get(1).contains("q'[a;b]'"));
        assertTrue(statements.get(1).endsWith("END;"));
        assertFalse(statements.get(1).contains("\n/"));
        assertEquals("SELECT q'{c;d}' FROM DUAL", statements.get(2));
    }

    private static OracleColumn column(String name, String type, String schema, String tableName) {
        OracleColumn column = new OracleColumn();
        column.setName(name);
        column.setType(type);
        column.setSchema(schema);
        column.setTableName(tableName);
        return column;
    }
}
